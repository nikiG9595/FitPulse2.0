package com.fitpulse.controller;

import com.fitpulse.config.AuthReturnNavigation;
import com.fitpulse.config.GoogleLogin;
import com.fitpulse.config.LegalTerms;
import com.fitpulse.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.csrf.HttpSessionCsrfTokenRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;

@Controller
public class GoogleLoginController {
    private final GoogleLogin google;
    private final UserService users;
    private final AuthReturnNavigation navigation;
    private final HttpSessionSecurityContextRepository securityContexts = new HttpSessionSecurityContextRepository();

    public GoogleLoginController(GoogleLogin google, UserService users, AuthReturnNavigation navigation) {
        this.google = google;
        this.users = users;
        this.navigation = navigation;
    }

    @GetMapping("/register/google")
    public String consent(HttpServletRequest request, Model model) {
        requireGoogle();
        if (request.getUserPrincipal() != null) return "redirect:/profile";
        var signup = google.pendingSignup(request);
        if (signup == null) return "redirect:/login?googleFailure=session";
        model.addAttribute("signup", signup);
        model.addAttribute("termsVersion", LegalTerms.VERSION);
        return "auth/google-consent";
    }

    @PostMapping("/register/google")
    public void signup(@RequestParam(defaultValue = "false") boolean termsAccepted,
                       @RequestParam(defaultValue = "") String termsVersion,
                       @RequestParam(required = false) String signupId,
                       HttpServletRequest request, HttpServletResponse response) throws IOException {
        requireGoogle();
        if (request.getUserPrincipal() != null) {
            response.sendRedirect(request.getContextPath() + "/profile");
            return;
        }
        if (!termsAccepted || !LegalTerms.VERSION.equals(termsVersion)) {
            response.sendRedirect(request.getContextPath() + "/register/google?googleTerms");
            return;
        }
        try {
            var authentication = google.completeSignup(request, signupId);
            request.changeSessionId();
            new HttpSessionCsrfTokenRepository().saveToken(null, request, response);
            var context = SecurityContextHolder.createEmptyContext();
            context.setAuthentication(authentication);
            SecurityContextHolder.setContext(context);
            securityContexts.saveContext(context, request, response);
            navigation.onAuthenticationSuccess(request, response, authentication);
        } catch (OAuth2AuthenticationException exception) {
            google.failure(request, response, exception);
        } catch (DataIntegrityViolationException exception) {
            response.sendRedirect(request.getContextPath() + "/login?googleFailure=account_conflict");
        }
    }

    @PostMapping("/profile/google")
    public void link(HttpServletRequest request, HttpServletResponse response) throws IOException {
        requireGoogle();
        response.sendRedirect(google.startLink(request, response, users.getCurrentUser().getId()));
    }

    private void requireGoogle() {
        if (!google.isEnabled()) throw new ResponseStatusException(HttpStatus.NOT_FOUND);
    }
}
