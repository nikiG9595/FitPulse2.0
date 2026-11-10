package com.fitpulse.controller;

import com.fitpulse.exception.FitPulseException;
import com.fitpulse.model.dto.ProfileUpdateRequest;
import com.fitpulse.service.UserService;
import com.fitpulse.repository.FavoriteGymRepository;
import com.fitpulse.repository.ProgressRecordRepository;
import com.fitpulse.repository.UserRepository;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
public class ProfileController {
    private final UserService userService;
    private final UserRepository userRepository;
    private final FavoriteGymRepository favoriteGymRepository;
    private final ProgressRecordRepository progressRecordRepository;

    public ProfileController(UserService userService, UserRepository userRepository,
                             FavoriteGymRepository favoriteGymRepository,
                             ProgressRecordRepository progressRecordRepository) {
        this.userService = userService;
        this.userRepository = userRepository;
        this.favoriteGymRepository = favoriteGymRepository;
        this.progressRecordRepository = progressRecordRepository;
    }

    @GetMapping("/profile")
    public String profile(Model model) {
        model.addAttribute("user", userService.getCurrentUser());
        model.addAttribute("favorites", favoriteGymRepository.findAllByUserOrderByCreatedAtDesc(userService.getCurrentUser()));
        return "profile";
    }

    @GetMapping("/profile/favorites")
    public String favorites(Model model) {
        model.addAttribute("favorites", favoriteGymRepository.findAllByUserOrderByCreatedAtDesc(userService.getCurrentUser()));
        return "profile-favorites";
    }

    @GetMapping("/users/{username}")
    public String publicProfile(@org.springframework.web.bind.annotation.PathVariable String username, Model model) {
        var user = userRepository.findByUsername(username)
                .filter(com.fitpulse.model.entity.User::isPublicProfile)
                .orElseThrow(() -> new FitPulseException("Публичният профил не е намерен."));
        model.addAttribute("user", user);
        model.addAttribute("favorites", user.isShowFavorites()
                ? favoriteGymRepository.findAllByUserOrderByCreatedAtDesc(user) : java.util.List.of());
        return "public-profile";
    }

    @GetMapping("/profile/edit")
    public String edit(Model model) {
        model.addAttribute("profileUpdateRequest", userService.getCurrentUserProfileUpdateRequest());
        return "profile-edit";
    }

    @PostMapping("/profile/edit")
    public String update(@Valid @ModelAttribute ProfileUpdateRequest profileUpdateRequest,
                         BindingResult bindingResult,
                         Model model) {
        if (bindingResult.hasErrors()) return "profile-edit";

        try {
            userService.updateCurrentUserProfile(profileUpdateRequest);
            return "redirect:/profile?updated";
        } catch (FitPulseException exception) {
            model.addAttribute("globalError", exception.getMessage());
            return "profile-edit";
        }
    }
}
