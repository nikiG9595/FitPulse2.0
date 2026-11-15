package com.fitpulse.config;

import com.fitpulse.service.SupportRateLimiter;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.multipart.MultipartException;
import org.springframework.web.multipart.support.StandardServletMultipartResolver;
import java.io.IOException;

/** Parse bounded uploads before CSRF reads form parameters; CSRF still runs for every accepted submission. */
@Component @Order(Ordered.HIGHEST_PRECEDENCE+20)
public class SupportUploadFilter extends OncePerRequestFilter {
    private final SupportRateLimiter limiter;
    private final StandardServletMultipartResolver multipart=new StandardServletMultipartResolver();
    public SupportUploadFilter(SupportRateLimiter limiter){this.limiter=limiter;}
    @Override protected boolean shouldNotFilter(HttpServletRequest request) {
        return !request.getMethod().equals("POST") || !request.getServletPath().equals("/support");
    }
    @Override protected void doFilterInternal(HttpServletRequest request,HttpServletResponse response,FilterChain chain)
            throws ServletException,IOException {
        // Do not use caller-controlled X-Forwarded-For headers as a rate-limit identity.
        if(!limiter.allow("ip:"+request.getRemoteAddr())) {
            response.setHeader("Retry-After","900");
            response.sendRedirect(request.getContextPath()+"/support?rateLimited");
            return;
        }
        if(!multipart.isMultipart(request)){chain.doFilter(request,response);return;}
        org.springframework.web.multipart.MultipartHttpServletRequest wrapped;
        try {wrapped=multipart.resolveMultipart(request);}
        catch(MultipartException e){response.sendRedirect(request.getContextPath()+"/support?uploadError");return;}
        try {chain.doFilter(wrapped,response);}
        finally {multipart.cleanupMultipart(wrapped);}
    }
}
