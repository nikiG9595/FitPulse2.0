package com.fitpulse.controller;

import com.fitpulse.config.SecurityConfig;
import com.fitpulse.exception.FitPulseException;
import com.fitpulse.model.entity.User;
import com.fitpulse.model.dto.ProfileUpdateRequest;
import com.fitpulse.service.UserService;
import com.fitpulse.repository.UserRepository;
import com.fitpulse.repository.FavoriteGymRepository;
import com.fitpulse.repository.ProgressRecordRepository;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrlPattern;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;

@WebMvcTest(ProfileController.class)
@Import(SecurityConfig.class)
class ProfileControllerSecurityTest {
    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserService userService;

    @MockitoBean private UserRepository userRepository;
    @MockitoBean private FavoriteGymRepository favoriteGymRepository;
    @MockitoBean private ProgressRecordRepository progressRecordRepository;

    @Test
    void authenticatedUserCanOpenEditProfilePage() throws Exception {
        when(userService.getCurrentUserProfileUpdateRequest()).thenReturn(profileRequest("member", "member@example.com"));

        mockMvc.perform(get("/profile/edit").with(user("member").roles("MEMBER")))
                .andExpect(status().isOk())
                .andExpect(view().name("profile-edit"));
    }

    @Test
    void authenticatedUserCanUpdateOwnProfile() throws Exception {
        mockMvc.perform(post("/profile/edit")
                        .with(user("member").roles("MEMBER"))
                        .with(csrf())
                        .param("username", "updatedMember")
                        .param("email", "updated@example.com"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/profile?updated"));

        ArgumentCaptor<ProfileUpdateRequest> captor = ArgumentCaptor.forClass(ProfileUpdateRequest.class);
        verify(userService).updateCurrentUserProfile(captor.capture());
        assertEquals("updatedMember", captor.getValue().getUsername());
        assertEquals("updated@example.com", captor.getValue().getEmail());
    }

    @Test
    void unauthenticatedAccessIsRedirectedToLogin() throws Exception {
        mockMvc.perform(get("/profile/edit"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("**/login"));
    }

    @Test
    void roleCannotBeSubmittedThroughProfileEditing() throws Exception {
        mockMvc.perform(post("/profile/edit")
                        .with(user("member").roles("MEMBER"))
                        .with(csrf())
                        .param("username", "member")
                        .param("email", "member@example.com")
                        .param("role", "ADMIN"))
                .andExpect(status().is3xxRedirection());

        ArgumentCaptor<ProfileUpdateRequest> captor = ArgumentCaptor.forClass(ProfileUpdateRequest.class);
        verify(userService).updateCurrentUserProfile(captor.capture());
        assertEquals(ProfileUpdateRequest.class, captor.getValue().getClass());
    }

    @Test
    void profilePageDisplaysCurrentUser() throws Exception {
        User currentUser = new User();
        currentUser.setRole(com.fitpulse.model.enums.UserRole.MEMBER);
        when(userService.getCurrentUser()).thenReturn(currentUser);
        mockMvc.perform(get("/profile").with(user("member").roles("MEMBER")))
                .andExpect(status().isOk()).andExpect(view().name("profile"))
                .andExpect(model().attribute("user", currentUser));
    }

    @Test
    void invalidProfileUpdateRedisplaysFormWithoutCallingService() throws Exception {
        mockMvc.perform(post("/profile/edit").with(user("member").roles("MEMBER")).with(csrf())
                        .param("username", "x").param("email", "invalid"))
                .andExpect(status().isOk()).andExpect(view().name("profile-edit"))
                .andExpect(model().attributeHasFieldErrors("profileUpdateRequest", "username", "email"));
        verify(userService, never()).updateCurrentUserProfile(org.mockito.ArgumentMatchers.any());
    }

    @Test
    void duplicateProfileDetailsAreShownAsFormError() throws Exception {
        doThrow(new FitPulseException("Email already exists"))
                .when(userService).updateCurrentUserProfile(org.mockito.ArgumentMatchers.any());
        mockMvc.perform(post("/profile/edit").with(user("member").roles("MEMBER")).with(csrf())
                        .param("username", "member").param("email", "used@example.com"))
                .andExpect(status().isOk()).andExpect(view().name("profile-edit"))
                .andExpect(model().attribute("globalError", "Email already exists"));
    }

    private ProfileUpdateRequest profileRequest(String username, String email) {
        ProfileUpdateRequest request = new ProfileUpdateRequest();
        request.setUsername(username);
        request.setEmail(email);
        return request;
    }
}
