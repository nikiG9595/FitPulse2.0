package com.fitpulse.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.multipart.MultipartException;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.multipart.support.StandardServletMultipartResolver;
import org.springframework.web.util.WebUtils;

import java.io.IOException;

final class ProfilePhotoUploadFilter extends OncePerRequestFilter {
    private final StandardServletMultipartResolver multipart = new StandardServletMultipartResolver();

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return !request.getMethod().equals("POST") || !request.getServletPath().equals("/profile/photo")
                || !multipart.isMultipart(request)
                || WebUtils.getNativeRequest(request, MultipartHttpServletRequest.class) != null;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            chain.doFilter(request, response);
            return;
        }

        // Make the bounded multipart form visible to Spring Security's CSRF filter.
        MultipartHttpServletRequest wrapped;
        try {
            wrapped = multipart.resolveMultipart(request);
        } catch (MultipartException e) {
            response.sendRedirect(request.getContextPath() + "/profile/edit?photoInvalid");
            return;
        }
        try {
            chain.doFilter(wrapped, response);
        } finally {
            multipart.cleanupMultipart(wrapped);
        }
    }
}