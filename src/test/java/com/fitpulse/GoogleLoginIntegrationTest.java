package com.fitpulse;

import com.fitpulse.config.GoogleLogin;
import com.fitpulse.config.LegalTerms;
import com.fitpulse.model.entity.User;
import com.fitpulse.model.enums.UserRole;
import com.fitpulse.repository.UserRepository;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserRequest;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.oidc.OidcIdToken;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.util.UriComponentsBuilder;
import org.springframework.web.util.UriUtils;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.hamcrest.Matchers.containsString;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.authenticated;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(properties = {
        "FITPULSE_GOOGLE_CLIENT_ID=test-client",
        "FITPULSE_GOOGLE_CLIENT_SECRET=test-secret",
        "spring.datasource.url=jdbc:h2:mem:fitpulse-google-test;MODE=MySQL;DATABASE_TO_LOWER=TRUE"
})
@AutoConfigureMockMvc
@ActiveProfiles("integration-test")
class GoogleLoginIntegrationTest {
    @Autowired GoogleLogin google;
    @Autowired UserRepository users;
    @Autowired MockMvc mvc;

    @AfterEach
    void clearContext() {
        RequestContextHolder.resetRequestAttributes();
        SecurityContextHolder.clearContext();
    }

    @Test
    void newAccountRequiresTermsThenSignsInBySubject() throws Exception {
        var identity = identity("new-subject", "New-Google@example.com", true);
        var callback = prepareCallback(mvc.perform(get("/oauth2/authorization/google")).andReturn());
        assertRejected(identity, "signup_required");
        assertThat(users.findByGoogleSubject("new-subject")).isEmpty();
        var response = new MockHttpServletResponse();
        google.failure(callback, response, new OAuth2AuthenticationException("signup_required"));
        assertThat(response.getRedirectedUrl()).isEqualTo("/register/google");
        var session = (MockHttpSession) callback.getSession(false);
        var signupId = google.pendingSignup(callback).id().toString();
        mvc.perform(get("/register/google").session(session))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("href=\"/terms\"")))
                .andExpect(content().string(containsString("href=\"/privacy\"")));
        mvc.perform(post("/register/google").session(session).with(csrf()))
                .andExpect(redirectedUrl("/register/google?googleTerms"));
        mvc.perform(post("/register/google").session(session).with(csrf())
                        .param("termsAccepted", "true").param("termsVersion", "old").param("signupId", signupId))
                .andExpect(redirectedUrl("/register/google?googleTerms"));
        mvc.perform(post("/register/google").session(session)
                        .param("termsAccepted", "true").param("termsVersion", LegalTerms.VERSION).param("signupId", signupId))
                .andExpect(status().isForbidden());
        mvc.perform(post("/register/google").session(session).with(csrf())
                        .param("termsAccepted", "true").param("termsVersion", LegalTerms.VERSION)
                        .param("signupId", "stale"))
                .andExpect(redirectedUrl("/login?googleFailure=session"));
        String oldSessionId = session.getId();
        mvc.perform(post("/register/google").session(session).with(csrf())
                        .param("termsAccepted", "true").param("termsVersion", LegalTerms.VERSION).param("signupId", signupId))
                .andExpect(redirectedUrl("/profile"))
                .andExpect(authenticated().withRoles("MEMBER"));
        assertThat(session.getId()).isNotEqualTo(oldSessionId);
        assertThat(google.pendingSignup(callback)).isNull();
        User account = users.findByGoogleSubject("new-subject").orElseThrow();
        assertThat(account.getEmail()).isEqualTo("new-google@example.com");
        assertThat(account.getAcceptedTermsVersion()).isEqualTo(LegalTerms.VERSION);
        assertThat(account.getTermsAcceptedAt()).isNotNull();
        mvc.perform(get("/profile").session(session))
                .andExpect(status().isOk())
                .andExpect(authenticated().withUsername(account.getUsername()));

        prepareCallback(mvc.perform(get("/oauth2/authorization/google")).andReturn());
        assertThat(google.authenticate(identity).getName()).isEqualTo(account.getUsername());
        assertThat(users.findTop2ByEmailIgnoreCase(account.getEmail())).hasSize(1);
    }

    @Test
    void bothPagesOfferTheSameGoogleEntryPoint() throws Exception {
        for (String page : List.of("/login", "/register")) {
            mvc.perform(get(page))
                    .andExpect(status().isOk())
                    .andExpect(content().string(containsString("href=\"/oauth2/authorization/google\"")));
        }
        mvc.perform(get("/register/google")).andExpect(redirectedUrl("/login?googleFailure=session"));
    }

    @Test
    void existingAdminIsLinkedOnlyFromSettingsAndRetainsRole() throws Exception {
        User admin = new User();
        admin.setUsername("google-test-admin");
        admin.setEmail("existing-google@example.com");
        admin.setRole(UserRole.ADMIN);
        admin.setPassword("existing-hash");
        users.saveAndFlush(admin);
        var identity = identity("admin-subject", "Existing-Google@example.com", true);

        prepareCallback(mvc.perform(get("/oauth2/authorization/google")).andReturn());
        assertRejected(identity, "account_exists");
        assertThat(users.findByGoogleSubject("admin-subject")).isEmpty();

        mvc.perform(post("/profile/google").with(user(admin.getUsername()).roles("ADMIN")))
                .andExpect(status().isForbidden());
        var link = mvc.perform(post("/profile/google").with(csrf()).with(user(admin.getUsername()).roles("ADMIN")))
                .andExpect(status().is3xxRedirection()).andReturn();
        prepareCallback(link);
        assertRejected(identity, "link_session_changed");
        SecurityContextHolder.getContext().setAuthentication(
                UsernamePasswordAuthenticationToken.authenticated(admin.getUsername(), null, List.of()));
        assertRejected(identity("admin-subject", "different@example.com", true), "email_mismatch");
        assertRejected(identity("admin-subject", admin.getEmail(), false), "email_unverified");

        var principal = google.authenticate(identity);
        assertThat(principal.getAuthorities()).extracting("authority").containsExactly("ROLE_ADMIN");
        var linked = users.findByGoogleSubject("admin-subject").orElseThrow();
        assertThat(linked.getId()).isEqualTo(admin.getId());
        assertThat(linked.getPassword()).isEqualTo("existing-hash");
    }

    @Test
    void callbackReportsDeniedAndExpiredSessionsSeparately() throws Exception {
        var start = mvc.perform(get("/oauth2/authorization/google")).andReturn();
        var callback = prepareCallback(start);
        mvc.perform(get("/login/oauth2/code/google").session((MockHttpSession) callback.getSession(false))
                        .param("state", callback.getParameter("state")).param("error", "access_denied"))
                .andExpect(redirectedUrl("/login?googleFailure=denied"));
        mvc.perform(get("/login/oauth2/code/google").param("state", "expired").param("code", "unused"))
                .andExpect(redirectedUrl("/login?googleFailure=session"));
        mvc.perform(get("/login").param("googleFailure", "account_exists")
                        .cookie(new Cookie("fitpulse-language", "en")))
                .andExpect(content().string(containsString("link Google in your profile settings")));
    }

    private MockHttpServletRequest prepareCallback(MvcResult start) {
        var authorization = UriComponentsBuilder.fromUriString(start.getResponse().getRedirectedUrl()).build();
        var callback = new MockHttpServletRequest("GET", "/login/oauth2/code/google");
        callback.setSession(start.getRequest().getSession(false));
        callback.setParameter("state",
                UriUtils.decode(authorization.getQueryParams().getFirst("state"), StandardCharsets.UTF_8));
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(callback, new MockHttpServletResponse()));
        return callback;
    }

    private OidcUserRequest identity(String subject, String email, boolean verified) {
        Instant now = Instant.now();
        var token = new OidcIdToken("id-token", now, now.plusSeconds(60),
                Map.of("sub", subject, "email", email, "email_verified", verified));
        var access = new OAuth2AccessToken(OAuth2AccessToken.TokenType.BEARER, "access-token",
                now, now.plusSeconds(60), Set.of("openid"));
        return new OidcUserRequest(google.clients().findByRegistrationId("google"), access, token);
    }

    private void assertRejected(OidcUserRequest identity, String code) {
        assertThatThrownBy(() -> google.authenticate(identity)).isInstanceOfSatisfying(
                OAuth2AuthenticationException.class, exception -> assertThat(exception.getError().getErrorCode()).isEqualTo(code));
    }
}
