package com.fitpulse.controller;

import com.fitpulse.exception.FitPulseException;
import com.fitpulse.model.dto.RegisterRequest;
import com.fitpulse.service.UserService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

@Controller
public class AuthController {
    private final UserService userService;
    private final com.fitpulse.config.AuthReturnNavigation navigation;

    public AuthController(UserService userService,com.fitpulse.config.AuthReturnNavigation navigation) {
        this.userService = userService;
        this.navigation=navigation;
    }

    @GetMapping("/login")
    public String login(jakarta.servlet.http.HttpServletRequest request,jakarta.servlet.http.HttpServletResponse response,Model model) {
        model.addAttribute("authReturnTo",navigation.capture(request,response));
        return "auth/login";
    }

    @GetMapping("/register")
    public String register(Model model,jakarta.servlet.http.HttpServletRequest request,jakarta.servlet.http.HttpServletResponse response) {
        model.addAttribute("authReturnTo",navigation.capture(request,response));
        model.addAttribute("registerRequest", new RegisterRequest());
        return "auth/register";
    }

    @PostMapping("/register")
    public String doRegister(@Valid @ModelAttribute RegisterRequest registerRequest, BindingResult bindingResult, Model model,jakarta.servlet.http.HttpServletRequest request,jakarta.servlet.http.HttpServletResponse response) {
        String destination=navigation.capture(request,response);model.addAttribute("authReturnTo",destination);
        if (bindingResult.hasErrors()) return "auth/register";
        try {
            userService.register(registerRequest);
            return "redirect:/login?registered&returnTo="+java.net.URLEncoder.encode(destination,java.nio.charset.StandardCharsets.UTF_8);
        } catch (FitPulseException e) {
            model.addAttribute("globalError", e.getMessage());
            return "auth/register";
        }
    }
}
