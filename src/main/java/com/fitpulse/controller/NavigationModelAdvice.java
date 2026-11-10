package com.fitpulse.controller;

import com.fitpulse.config.AuthReturnNavigation;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice
public class NavigationModelAdvice {
    private final AuthReturnNavigation navigation;
    public NavigationModelAdvice(AuthReturnNavigation navigation){this.navigation=navigation;}
    @ModelAttribute("authReturnTo") public String returnTo(HttpServletRequest request){
        if(request.getServletPath().equals("/login")||request.getServletPath().equals("/register"))return navigation.pending(request);
        String value=navigation.internal(request.getRequestURI()+(request.getQueryString()==null?"":"?"+request.getQueryString()),request);
        return value==null?"/":value;
    }
}
