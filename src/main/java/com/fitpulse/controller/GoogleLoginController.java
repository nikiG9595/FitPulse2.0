package com.fitpulse.controller;

import com.fitpulse.config.GoogleLogin;
import com.fitpulse.config.LegalTerms;
import com.fitpulse.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;

@Controller
public class GoogleLoginController {
    private final GoogleLogin google;
    private final UserService users;

    public GoogleLoginController(GoogleLogin google, UserService users) {
        this.google = google;
        this.users = users;
    }

    @PostMapping("/register/google")
    public void signup(@RequestParam(defaultValue = "false") boolean termsAccepted,
                       @RequestParam(defaultValue = "") String termsVersion,
                       HttpServletRequest request, HttpServletResponse response) throws IOException {
        requireGoogle();
        if (!termsAccepted || !LegalTerms.VERSION.equals(termsVersion)) {
            response.sendRedirect(request.getContextPath() + "/register?googleTerms");
            return;
        }
        response.sendRedirect(google.start(request, response, true, null));
    }

    @PostMapping("/profile/google")
    public void link(HttpServletRequest request, HttpServletResponse response) throws IOException {
        requireGoogle();
        response.sendRedirect(google.start(request, response, false, users.getCurrentUser().getId()));
    }

    private void requireGoogle() {
        if (!google.isEnabled()) throw new ResponseStatusException(HttpStatus.NOT_FOUND);
    }
}