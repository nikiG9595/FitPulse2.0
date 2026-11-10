package com.fitpulse.controller;

import com.fitpulse.exception.FitPulseException;
import com.fitpulse.repository.UserRepository;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import java.util.UUID;

@Controller
public class AdminProfileModerationController {
    private final UserRepository users;
    public AdminProfileModerationController(UserRepository users) { this.users = users; }
    @Transactional
    @PostMapping("/admin/users/{id}/hide-profile")
    public String hide(@PathVariable UUID id, RedirectAttributes attributes) {
        users.findById(id).orElseThrow(() -> new FitPulseException("Потребителят не е намерен.")).setPublicProfile(false);
        attributes.addFlashAttribute("success", "Публичният профил е скрит.");
        return "redirect:/admin/users";
    }
}
