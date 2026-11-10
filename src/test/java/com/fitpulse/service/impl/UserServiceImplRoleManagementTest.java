package com.fitpulse.service.impl;

import com.fitpulse.exception.FitPulseException;
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
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class UserServiceImplRoleManagementTest {
    private UserRepository userRepository;
    private UserServiceImpl userService;

    @BeforeEach
    void setUp() {
        userRepository = mock(UserRepository.class);
        userService = new UserServiceImpl(userRepository, mock(PasswordEncoder.class));
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("admin", "password"));
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void adminCanChangeAnotherUsersRole() {
        UUID adminId = UUID.randomUUID();
        UUID memberId = UUID.randomUUID();
        User admin = user(adminId, "admin", UserRole.ADMIN);
        User member = user(memberId, "member", UserRole.MEMBER);
        when(userRepository.findById(memberId)).thenReturn(Optional.of(member));
        when(userRepository.findByUsername("admin")).thenReturn(Optional.of(admin));

        userService.changeRole(memberId, UserRole.ADMIN);

        verify(member).setRole(UserRole.ADMIN);
        verify(userRepository).save(member);
    }

    @Test
    void adminCannotRemoveOwnAdminRole() {
        UUID adminId = UUID.randomUUID();
        User admin = user(adminId, "admin", UserRole.ADMIN);
        when(userRepository.findById(adminId)).thenReturn(Optional.of(admin));
        when(userRepository.findByUsername("admin")).thenReturn(Optional.of(admin));

        FitPulseException exception = assertThrows(FitPulseException.class,
                () -> userService.changeRole(adminId, UserRole.MEMBER));

        assertEquals("Не можете да премахнете собствената си администраторска роля.", exception.getMessage());
        assertEquals(UserRole.ADMIN, admin.getRole());
        verify(userRepository, never()).save(admin);
    }

    private User user(UUID id, String username, UserRole role) {
        User user = mock(User.class);
        when(user.getId()).thenReturn(id);
        when(user.getUsername()).thenReturn(username);
        when(user.getRole()).thenReturn(role);
        return user;
    }
}
