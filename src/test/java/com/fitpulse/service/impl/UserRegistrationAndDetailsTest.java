package com.fitpulse.service.impl;

import com.fitpulse.exception.FitPulseException;
import com.fitpulse.model.dto.RegisterRequest;
import com.fitpulse.model.entity.User;
import com.fitpulse.model.enums.UserRole;
import com.fitpulse.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class UserRegistrationAndDetailsTest {
    private UserRepository repository;
    private PasswordEncoder encoder;
    private UserServiceImpl service;

    @BeforeEach void setUp() { repository = mock(UserRepository.class); encoder = mock(PasswordEncoder.class); service = new UserServiceImpl(repository, encoder); }

    @Test
    void registrationValidatesPasswordsUsernameAndEmail() {
        RegisterRequest request = request(); request.setConfirmPassword("different");
        assertMessage("Паролите не съвпадат.", () -> service.register(request));
        request.setConfirmPassword("secret1");
        when(repository.existsByUsername("newuser")).thenReturn(true);
        assertMessage("Потребителското име вече е заето.", () -> service.register(request));
        when(repository.existsByUsername("newuser")).thenReturn(false);
        when(repository.existsByEmail("new@example.com")).thenReturn(true);
        assertMessage("Имейл адресът вече е регистриран.", () -> service.register(request));
    }

    @Test
    void successfulRegistrationEncodesPasswordAndAssignsMemberRole() {
        RegisterRequest request = request();
        when(encoder.encode("secret1")).thenReturn("encoded");
        service.register(request);
        verify(repository).save(argThat(user -> user.getUsername().equals("newuser")
                && user.getEmail().equals("new@example.com") && user.getPassword().equals("encoded")
                && user.getRole() == UserRole.MEMBER));
    }

    @Test
    void getAllUsersDelegates() {
        List<User> users = List.of(new User()); when(repository.findAll()).thenReturn(users);
        assertSame(users, service.getAllUsers());
    }

    @Test
    void userDetailsLoadsSecurityPrincipalAndRejectsUnknownUser() {
        User user = new User(); user.setUsername("member"); user.setPassword("hash"); user.setRole(UserRole.MEMBER);
        when(repository.findByUsername("member")).thenReturn(Optional.of(user));
        UserDetailsServiceImpl detailsService = new UserDetailsServiceImpl(repository);
        var details = detailsService.loadUserByUsername("member");
        assertEquals("member", details.getUsername());
        assertTrue(details.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_MEMBER")));
        assertThrows(UsernameNotFoundException.class, () -> detailsService.loadUserByUsername("missing"));
    }

    private static RegisterRequest request() {
        RegisterRequest r = new RegisterRequest(); r.setUsername("newuser"); r.setEmail("new@example.com");
        r.setPassword("secret1"); r.setConfirmPassword("secret1"); return r;
    }

    private static void assertMessage(String message, Runnable action) {
        assertEquals(message, assertThrows(FitPulseException.class, action::run).getMessage());
    }
}
