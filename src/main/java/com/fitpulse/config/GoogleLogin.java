package com.fitpulse.config;

import com.fitpulse.model.entity.User;
import com.fitpulse.model.enums.UserRole;
import com.fitpulse.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.config.oauth2.client.CommonOAuth2Provider;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserRequest;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserService;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.registration.InMemoryClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.DefaultOAuth2AuthorizationRequestResolver;
import org.springframework.security.oauth2.client.web.HttpSessionOAuth2AuthorizationRequestRepository;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.time.Instant;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

@Component
public class GoogleLogin {
    private static final Logger log = LoggerFactory.getLogger(GoogleLogin.class);
    private static final String INTENT = GoogleLogin.class.getName();
    private final ClientRegistrationRepository clients;
    private final UserRepository users;
    private final PasswordEncoder passwords;
    private final OidcUserService identities = new OidcUserService();
    private final HttpSessionOAuth2AuthorizationRequestRepository requests =
            new HttpSessionOAuth2AuthorizationRequestRepository();

    public GoogleLogin(@Value("${FITPULSE_GOOGLE_CLIENT_ID:}") String clientId,
                       @Value("${FITPULSE_GOOGLE_CLIENT_SECRET:}") String secret,
                       @Value("${FITPULSE_GOOGLE_REDIRECT_URI:{baseUrl}/login/oauth2/code/google}") String redirectUri,
                       UserRepository users, PasswordEncoder passwords) {
        this.users = users;
        this.passwords = passwords;
        this.clients = clientId.isBlank() || secret.isBlank() ? null : new InMemoryClientRegistrationRepository(
                CommonOAuth2Provider.GOOGLE.getBuilder("google").clientId(clientId).clientSecret(secret)
                        .scope("openid", "email").redirectUri(redirectUri).build());
    }

    public boolean isEnabled() { return clients != null; }
    public ClientRegistrationRepository clients() { return clients; }

    public String start(HttpServletRequest request, HttpServletResponse response, boolean signup, UUID linkTo) {
        var resolver = new DefaultOAuth2AuthorizationRequestResolver(clients, "/oauth2/authorization");
        var authorization = resolver.resolve(request, "google");
        requests.saveAuthorizationRequest(authorization, request, response);
        request.getSession().setAttribute(INTENT, new Intent(authorization.getState(), signup, linkTo));
        return authorization.getAuthorizationRequestUri();
    }

    private record Intent(String state, boolean signup, UUID linkTo) implements java.io.Serializable {}

    private Intent intent(HttpServletRequest request) {
        var session = request.getSession(false);
        if (session == null || !(session.getAttribute(INTENT) instanceof Intent intent)) return null;
        return intent.state().equals(request.getParameter("state")) ? intent : null;
    }

    public boolean finish(HttpServletRequest request) {
        Intent intent = intent(request);
        if (intent == null) return false;
        request.getSession(false).removeAttribute(INTENT);
        return intent.linkTo() != null;
    }

    public void failure(HttpServletRequest request, HttpServletResponse response,
                        org.springframework.security.core.AuthenticationException exception) throws java.io.IOException {
        Intent intent = intent(request);
        finish(request);
        String code = exception instanceof OAuth2AuthenticationException oauth ? oauth.getError().getErrorCode() : "";
        String reason = switch (code) {
            case "signup_required", "account_exists", "email_unverified", "email_mismatch",
                    "google_already_linked", "account_already_linked", "account_conflict" -> code;
            case "authorization_request_not_found", "invalid_state_parameter", "link_session_changed" -> "session";
            case "access_denied" -> "denied";
            case "invalid_client", "unauthorized_client", "redirect_uri_mismatch" -> "configuration";
            case "invalid_grant", "invalid_token_response" -> "token_exchange";
            case "invalid_id_token", "invalid_nonce", "invalid_user_info_response", "invalid_google_subject" -> "identity";
            default -> "provider";
        };
        // Log the classified failure only. Provider error descriptions can contain sensitive data.
        log.warn("Google OAuth callback failed: reason={}, flow={}", reason,
                intent == null ? "login" : intent.linkTo() == null ? "signup" : "link");
        String page = intent != null && intent.linkTo() != null ? "/profile/edit"
                : "signup_required".equals(reason) || intent != null && intent.signup() ? "/register" : "/login";
        response.sendRedirect(response.encodeRedirectURL(request.getContextPath() + page + "?googleFailure=" + reason));
    }

    public String failureMessage(String[] values) {
        if (values == null || values.length != 1) return null;
        return switch (values[0]) {
            case "signup_required", "account_exists", "email_unverified", "email_mismatch",
                    "google_already_linked", "account_already_linked", "account_conflict",
                    "session", "denied", "configuration", "token_exchange", "identity", "provider" ->
                    "auth.google.failure." + values[0];
            default -> null;
        };
    }

    @org.springframework.transaction.annotation.Transactional
    public OidcUser authenticate(OidcUserRequest request) {
        var servletRequest = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();
        Intent intent = intent(servletRequest);
        OidcUser google = identities.loadUser(request);
        String subject = google.getSubject();
        if (subject == null || subject.isBlank() || subject.length() > 255) throw rejected("invalid_google_subject");
        try {
            User user = resolveAccount(google, intent);
            return new FitPulseGooglePrincipal(user.getUsername(), user.getRole(), google);
        } catch (DataIntegrityViolationException e) {
            throw rejected("account_conflict");
        }
    }

    private User resolveAccount(OidcUser google, Intent intent) {
        String subject = google.getSubject();
        var existing = users.findByGoogleSubject(subject);
        if (intent == null || intent.linkTo() == null) {
            if (existing.isPresent()) return existing.get();
            String email = verifiedEmail(google);
            if (!users.findTop2ByEmailIgnoreCase(email).isEmpty()) throw rejected("account_exists");
            if (intent == null || !intent.signup()) throw rejected("signup_required");
            User user = new User();
            user.setUsername("google-" + UUID.randomUUID().toString().replace("-", "").substring(0, 22));
            user.setEmail(email);
            user.setPassword(passwords.encode(UUID.randomUUID().toString()));
            user.setRole(UserRole.MEMBER);
            user.setGoogleSubject(subject);
            user.setWelcomeShown(false);
            user.acceptTerms(LegalTerms.VERSION, Instant.now());
            return users.saveAndFlush(user);
        }
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()
                || authentication instanceof org.springframework.security.authentication.AnonymousAuthenticationToken) {
            throw rejected("link_session_changed");
        }
        User current = users.lockForLibrary(intent.linkTo()).orElseThrow(() -> rejected("link_session_changed"));
        if (!current.getUsername().equals(authentication.getName())) throw rejected("link_session_changed");
        if (!current.getEmail().equalsIgnoreCase(verifiedEmail(google))) throw rejected("email_mismatch");
        if (existing.filter(user -> !user.getId().equals(current.getId())).isPresent()) {
            throw rejected("google_already_linked");
        }
        if (current.getGoogleSubject() != null && !current.getGoogleSubject().equals(subject)) {
            throw rejected("account_already_linked");
        }
        current.setGoogleSubject(subject);
        return users.saveAndFlush(current);
    }

    private static String verifiedEmail(OidcUser google) {
        String email = google.getEmail();
        if (!Boolean.TRUE.equals(google.getEmailVerified()) || email == null || email.isBlank() || email.length() > 255) {
            throw rejected("email_unverified");
        }
        return email.strip().toLowerCase(Locale.ROOT);
    }

    private static final class FitPulseGooglePrincipal extends DefaultOidcUser {
        private final String username;

        FitPulseGooglePrincipal(String username, UserRole role, OidcUser google) {
            super(List.of(new SimpleGrantedAuthority("ROLE_" + role.name())), google.getIdToken(), google.getUserInfo());
            this.username = username;
        }

        @Override
        public String getName() { return username; }
    }

    private static OAuth2AuthenticationException rejected(String code) {
        return new OAuth2AuthenticationException(code);
    }
}
