package com.fitpulse.controller;

import com.fitpulse.service.UserService;
import com.fitpulse.service.GymDirectoryService;
import com.fitpulse.service.WellnessService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    private final WellnessService wellness;
    private final UserService userService;
    private final GymDirectoryService gymDirectoryService;

    public HomeController(UserService userService, GymDirectoryService gymDirectoryService, WellnessService wellness) {
        this.wellness = wellness;
        this.userService = userService;
        this.gymDirectoryService = gymDirectoryService;
    }

    @GetMapping("/")
    public String home(Model model) {
        model.addAttribute("gyms", gymDirectoryService.search(null, null, 0).getContent().stream().limit(3).toList());
        return "home";
    }

    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        model.addAttribute("user", userService.getCurrentUser());

        model.addAttribute("upcoming",wellness.upcoming());
        model.addAttribute("reminders",wellness.reminders());
        model.addAttribute("programs",wellness.myPrograms().stream().filter(p->p.isActive()).toList());
        model.addAttribute("mealPlans",wellness.myMeals());
        return "dashboard";
    }
}
