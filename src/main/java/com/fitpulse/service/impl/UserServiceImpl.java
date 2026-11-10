package com.fitpulse.service.impl;

import com.fitpulse.exception.FitPulseException;
import com.fitpulse.model.dto.ProfileUpdateRequest;
import com.fitpulse.model.dto.RegisterRequest;
import com.fitpulse.model.entity.User;
import com.fitpulse.model.enums.UserRole;
import com.fitpulse.repository.UserRepository;
import com.fitpulse.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class UserServiceImpl implements UserService {
    private static final Logger LOGGER = LoggerFactory.getLogger(UserServiceImpl.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserServiceImpl(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void register(RegisterRequest request) {
        if (!request.getPassword().equals(request.getConfirmPassword()))
            throw new FitPulseException("Паролите не съвпадат.");
        if (userRepository.existsByUsername(request.getUsername()))
            throw new FitPulseException("Потребителското име вече е заето.");
        if (userRepository.existsByEmail(request.getEmail())) throw new FitPulseException("Имейл адресът вече е регистриран.");
        User user = new User();
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole(UserRole.MEMBER);
        userRepository.save(user);
        LOGGER.info("User registered: userId={}", user.getId());
    }

    @Override
    public User getCurrentUser() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByUsername(username).orElseThrow(() -> new FitPulseException("Текущият потребител не е намерен."));
    }

    @Override
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    @Override
    public void changeRole(UUID userId, UserRole newRole) {
        if (userId == null) throw new FitPulseException("Необходим е валиден идентификатор на потребител.");
        if (newRole == null) throw new FitPulseException("Изберете валидна роля.");

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new FitPulseException("Потребителят не е намерен."));
        User currentUser = getCurrentUser();

        if (user.getId().equals(currentUser.getId()) && user.getRole() == UserRole.ADMIN && newRole != UserRole.ADMIN)
            throw new FitPulseException("Не можете да премахнете собствената си администраторска роля.");

        UserRole previousRole = user.getRole();
        if (previousRole == newRole) throw new FitPulseException("Потребителят вече има избраната роля.");

        user.setRole(newRole);
        userRepository.save(user);
        LOGGER.info("User role changed: userId={}, previousRole={}, newRole={}, changedByUserId={}",
                user.getId(), previousRole, newRole, currentUser.getId());
    }

    @Override
    public ProfileUpdateRequest getCurrentUserProfileUpdateRequest() {
        User currentUser = getCurrentUser();
        ProfileUpdateRequest request = new ProfileUpdateRequest();
        request.setUsername(currentUser.getUsername());
        request.setEmail(currentUser.getEmail());
        request.setDisplayName(currentUser.getDisplayName());
        request.setBio(currentUser.getBio());
        request.setHomeCity(currentUser.getHomeCity());
        request.setPublicProfile(currentUser.isPublicProfile());
        request.setShowCity(currentUser.isShowCity());
        request.setShowFavorites(currentUser.isShowFavorites());
        return request;
    }

    @Override
    public void updateCurrentUserProfile(ProfileUpdateRequest request) {
        if (request == null) throw new FitPulseException("Данните за профила са задължителни.");

        User currentUser = getCurrentUser();
        if (userRepository.existsByUsernameAndIdNot(request.getUsername(), currentUser.getId()))
            throw new FitPulseException("Потребителското име вече е заето.");
        if (userRepository.existsByEmailAndIdNot(request.getEmail(), currentUser.getId()))
            throw new FitPulseException("Имейл адресът вече е регистриран.");

        currentUser.setUsername(request.getUsername());
        currentUser.setEmail(request.getEmail());
        currentUser.setDisplayName(request.getDisplayName());
        currentUser.setBio(request.getBio());
        currentUser.setHomeCity(request.getHomeCity());
        currentUser.setPublicProfile(request.isPublicProfile());
        currentUser.setShowCity(request.isShowCity());
        currentUser.setShowFavorites(request.isShowFavorites());
        userRepository.save(currentUser);
        refreshAuthenticationUsername(request.getUsername());
        LOGGER.info("User profile updated: userId={}", currentUser.getId());
    }

    private void refreshAuthenticationUsername(String username) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UsernamePasswordAuthenticationToken updatedAuthentication = UsernamePasswordAuthenticationToken.authenticated(
                username, authentication.getCredentials(), authentication.getAuthorities());
        updatedAuthentication.setDetails(authentication.getDetails());
        SecurityContextHolder.getContext().setAuthentication(updatedAuthentication);
    }
}
