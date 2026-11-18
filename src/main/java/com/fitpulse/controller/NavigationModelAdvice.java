package com.fitpulse.controller;

import com.fitpulse.config.AuthReturnNavigation;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice
public class NavigationModelAdvice {
    private final AuthReturnNavigation navigation;

    public NavigationModelAdvice(AuthReturnNavigation navigation) {
        this.navigation = navigation;
    }

    @org.springframework.beans.factory.annotation.Autowired
    private com.fitpulse.service.GymDirectoryService gyms;
    @org.springframework.beans.factory.annotation.Autowired
    private com.fitpulse.service.SupportService support;

    @ModelAttribute("adminTaskIndicator")
    public java.util.function.BooleanSupplier adminTaskIndicator(org.springframework.security.core.Authentication authentication) {
        boolean admin=authentication!=null && authentication.getAuthorities().stream().anyMatch(a->a.getAuthority().equals("ROLE_ADMIN"));
        // Thymeleaf evaluates this after the controller has opened/processed a task.
        return () -> admin && (gyms.pendingCount()>0 || support.unreadCount()>0 || gyms.newCommentCount()>0 || gyms.reportCount()>0);
    }

    @ModelAttribute("loginToast")
    public Object loginToast(HttpServletRequest request) {
        var session=request.getSession(false);
        if(session==null)return null;
        Object value=session.getAttribute("FITPULSE_LOGIN_TOAST");
        session.removeAttribute("FITPULSE_LOGIN_TOAST");
        return value;
    }

    @ModelAttribute("showFitPulseBanner")
    public boolean showFitPulseBanner(HttpServletRequest request) {
        String path = request.getRequestURI().substring(request.getContextPath().length());
        return !path.equals("/") && !path.equals("/login") && !path.equals("/register");
    }

    @ModelAttribute("authReturnTo")
    public String returnTo(HttpServletRequest request) {
        if (request.getServletPath().equals("/login") || request.getServletPath().equals("/register"))
            return navigation.pending(request);
        String value = navigation.internal(request.getRequestURI() + (request.getQueryString() == null ? "" : "?" + request.getQueryString()), request);
        return value == null ? "/" : value;
    }
}
