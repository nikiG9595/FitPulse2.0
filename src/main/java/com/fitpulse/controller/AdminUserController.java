package com.fitpulse.controller;

import com.fitpulse.exception.FitPulseException;
import com.fitpulse.model.dto.RoleChangeRequest;
import com.fitpulse.model.enums.UserRole;
import com.fitpulse.service.UserService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.UUID;

@Controller
@RequestMapping("/admin/users")
public class AdminUserController {
    private final UserService userService;

    public AdminUserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    public String all(Model model) {
        model.addAttribute("users", userService.getAllUsers());
        model.addAttribute("roles", UserRole.values());
        return "admin/users";
    }

    @PostMapping("/{id}/role")
    public String changeRole(@PathVariable String id,
                             @Valid @ModelAttribute RoleChangeRequest roleChangeRequest,
                             BindingResult bindingResult,
                             RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("error", "Ролята трябва да бъде потребител или администратор.");
            return "redirect:/admin/users";
        }

        try {
            userService.changeRole(UUID.fromString(id), roleChangeRequest.getRole());
            redirectAttributes.addFlashAttribute("success", "Ролята на потребителя е променена успешно.");
        } catch (IllegalArgumentException exception) {
            redirectAttributes.addFlashAttribute("error", "Невалиден идентификатор на потребител.");
        } catch (FitPulseException exception) {
            redirectAttributes.addFlashAttribute("error", exception.getMessage());
        }
        return "redirect:/admin/users";
    }
}
