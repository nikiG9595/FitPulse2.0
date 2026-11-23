package com.fitpulse.service;

import com.fitpulse.model.dto.ProfileUpdateRequest;
import com.fitpulse.model.dto.RegisterRequest;
import com.fitpulse.model.entity.User;
import com.fitpulse.model.enums.UserRole;

import java.util.List;
import java.util.UUID;

public interface UserService {
    void register(RegisterRequest request);

    User getCurrentUser();

    boolean consumeFirstLogin();

    boolean changePassword(String current, String replacement, String confirmation);

    List<User> getAllUsers();

    void changeRole(UUID userId, UserRole newRole);

    ProfileUpdateRequest getCurrentUserProfileUpdateRequest();

    void updateCurrentUserProfile(ProfileUpdateRequest request);
}
