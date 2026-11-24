package com.fitpulse.controller;

import com.fitpulse.exception.FitPulseException;
import com.fitpulse.model.dto.ProfileUpdateRequest;
import com.fitpulse.repository.FavoriteGymRepository;
import com.fitpulse.repository.ProgressRecordRepository;
import com.fitpulse.repository.UserRepository;
import com.fitpulse.service.UserService;
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
    public String profile(Model model, jakarta.servlet.http.HttpSession session) {
        model.addAttribute("newUser", session.getAttribute("FITPULSE_NEW_USER")!=null);
        session.removeAttribute("FITPULSE_NEW_USER");
        var storedProfile=userService.getCurrentUserProfileUpdateRequest();
        model.addAttribute("personalDataSummary",storedProfile);
        if (storedProfile.getBirthDate() != null) {
            model.addAttribute("profileAge", java.time.Period.between(
                    storedProfile.getBirthDate(), java.time.LocalDate.now()).getYears());
        }
        if(!model.containsAttribute("profileUpdateRequest")) model.addAttribute("profileUpdateRequest", storedProfile);
        model.addAttribute("profileComplete",storedProfile.getSex()!=null && storedProfile.getBirthDate()!=null && storedProfile.getHeight()!=null && storedProfile.getWeight()!=null);
        model.addAttribute("user", userService.getCurrentUser());
        model.addAttribute("favorites", favoriteGymRepository.findAllByUserOrderByCreatedAtDesc(userService.getCurrentUser()));
        return "profile";
    }

    @org.springframework.beans.factory.annotation.Autowired
    private jakarta.validation.Validator validator;

    @PostMapping("/profile/personal-data")
    public String personalData(@ModelAttribute("profileUpdateRequest") ProfileUpdateRequest input,
                               BindingResult binding, Model model, jakarta.servlet.http.HttpSession session) {
        ProfileUpdateRequest stored=userService.getCurrentUserProfileUpdateRequest();
        stored.setSex(input.getSex());stored.setBirthDate(input.getBirthDate());
        stored.setHeight(input.getHeight());stored.setWeight(input.getWeight());
        for(var violation:validator.validate(stored)) {
            String field=violation.getPropertyPath().toString();
            if(!binding.hasFieldErrors(field))binding.rejectValue(field,"invalid",violation.getMessage());
        }
        if(binding.hasErrors())return profile(model,session);
        try {
            userService.updateCurrentUserProfile(stored);
            return "redirect:/profile?updated";
        } catch(FitPulseException exception) {
            model.addAttribute("globalError",exception.getMessage());
            return profile(model,session);
        }
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

    @PostMapping("/profile/password")
    public String changePassword(@org.springframework.web.bind.annotation.RequestParam String currentPassword,
                                 @org.springframework.web.bind.annotation.RequestParam String newPassword,
                                 @org.springframework.web.bind.annotation.RequestParam String confirmPassword,
                                 org.springframework.web.servlet.mvc.support.RedirectAttributes flash) {
        boolean updated = userService.changePassword(currentPassword, newPassword, confirmPassword);
        flash.addFlashAttribute(updated ? "passwordSuccess" : "passwordError", true);
        return "redirect:/profile/edit#password-settings";
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
