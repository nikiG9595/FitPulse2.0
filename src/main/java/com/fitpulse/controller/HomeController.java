package com.fitpulse.controller;

import com.fitpulse.service.UserService;
import com.fitpulse.service.WellnessService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    private final WellnessService wellness;
    private final UserService userService;

    public HomeController(UserService userService, WellnessService wellness) {
        this.wellness = wellness;
        this.userService = userService;
    }

    @GetMapping("/")
    public String home() {
        return "home";
    }

    @GetMapping("/about")
    public String about() {
        return "about";
    }

    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        model.addAttribute("user", userService.getCurrentUser());

        model.addAttribute("upcoming", wellness.upcoming());
        model.addAttribute("reminders", wellness.reminders());
        model.addAttribute("programs", wellness.myPrograms().stream().filter(p -> p.isActive()).toList());
        model.addAttribute("mealPlans", wellness.myMeals());
        return "dashboard";
    }
}
