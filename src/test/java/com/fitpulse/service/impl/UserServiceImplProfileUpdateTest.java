package com.fitpulse.service.impl;

import com.fitpulse.exception.FitPulseException;
import com.fitpulse.model.dto.ProfileUpdateRequest;
import com.fitpulse.model.entity.User;
import com.fitpulse.model.enums.UserRole;
import com.fitpulse.repository.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class UserServiceImplProfileUpdateTest {
    private UserRepository userRepository;
    private UserServiceImpl userService;
    private User currentUser;

    @BeforeEach
    void setUp() {
        userRepository = mock(UserRepository.class);
        userService = new UserServiceImpl(userRepository, mock(PasswordEncoder.class));
        currentUser = new User();
        currentUser.setUsername("member");
        currentUser.setEmail("member@example.com");
        currentUser.setRole(UserRole.MEMBER);
        when(userRepository.findByUsername("member")).thenReturn(Optional.of(currentUser));
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("member", "password"));
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void authenticatedUserCanUpdateOwnProfileWithoutChangingRole() {
        ProfileUpdateRequest request = request("updatedMember", "updated@example.com");

        userService.updateCurrentUserProfile(request);

        assertEquals("updatedMember", currentUser.getUsername());
        assertEquals("updated@example.com", currentUser.getEmail());
        assertEquals(UserRole.MEMBER, currentUser.getRole());
        assertEquals("updatedMember", SecurityContextHolder.getContext().getAuthentication().getName());
        verify(userRepository).save(currentUser);
    }

    @Test
    void duplicateUsernameIsRejected() {
        ProfileUpdateRequest request = request("existingUser", "member@example.com");
        when(userRepository.existsByUsernameAndIdNot("existingUser", currentUser.getId())).thenReturn(true);

        FitPulseException exception = assertThrows(FitPulseException.class,
                () -> userService.updateCurrentUserProfile(request));

        assertEquals("Потребителското име вече е заето.", exception.getMessage());
        verify(userRepository, never()).save(currentUser);
    }

    @Test
    void duplicateEmailIsRejected() {
        ProfileUpdateRequest request = request("member", "existing@example.com");
        when(userRepository.existsByEmailAndIdNot("existing@example.com", currentUser.getId())).thenReturn(true);

        FitPulseException exception = assertThrows(FitPulseException.class,
                () -> userService.updateCurrentUserProfile(request));

        assertEquals("Имейл адресът вече е регистриран.", exception.getMessage());
        verify(userRepository, never()).save(currentUser);
    }

    private ProfileUpdateRequest request(String username, String email) {
        ProfileUpdateRequest request = new ProfileUpdateRequest();
        request.setUsername(username);
        request.setEmail(email);
        return request;
    }
}
