package com.fitpulse.controller;

import com.fitpulse.config.SecurityConfig;
import com.fitpulse.exception.FitPulseException;
import com.fitpulse.model.enums.UserRole;
import com.fitpulse.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.UUID;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.flash;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;

@WebMvcTest(AdminUserController.class)
@Import(SecurityConfig.class)
class AdminUserControllerSecurityTest {
    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserService userService;

    @Test
    void adminCanAccessUserManagementPage() throws Exception {
        when(userService.getAllUsers()).thenReturn(List.of());

        mockMvc.perform(get("/admin/users").with(user("admin").roles("ADMIN")))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/users"));
    }

    @Test
    void memberReceivesForbiddenAccess() throws Exception {
        mockMvc.perform(get("/admin/users").with(user("member").roles("MEMBER")))
                .andExpect(status().isForbidden());
    }

    @Test
    void adminCanChangeAnotherUsersRole() throws Exception {
        UUID userId = UUID.randomUUID();

        mockMvc.perform(post("/admin/users/{id}/role", userId)
                        .with(user("admin").roles("ADMIN"))
                        .with(csrf())
                        .param("role", "ADMIN"))
                .andExpect(status().is3xxRedirection());

        verify(userService).changeRole(userId, UserRole.ADMIN);
    }

    @Test
    void missingRoleIsRejectedBeforeCallingService() throws Exception {
        mockMvc.perform(post("/admin/users/{id}/role", UUID.randomUUID())
                        .with(user("admin").roles("ADMIN")).with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/users"))
                .andExpect(flash().attribute("error", "Ролята трябва да бъде потребител или администратор."));
        verify(userService, never()).changeRole(any(), any());
    }

    @Test
    void malformedUserIdProducesSafeFlashError() throws Exception {
        mockMvc.perform(post("/admin/users/{id}/role", "not-a-uuid")
                        .with(user("admin").roles("ADMIN")).with(csrf()).param("role", "MEMBER"))
                .andExpect(status().is3xxRedirection())
                .andExpect(flash().attribute("error", "Невалиден идентификатор на потребител."));
    }

    @Test
    void roleBusinessRuleFailureIsReportedToAdmin() throws Exception {
        UUID id = UUID.randomUUID();
        doThrow(new FitPulseException("You cannot remove your own ADMIN role"))
                .when(userService).changeRole(id, UserRole.MEMBER);
        mockMvc.perform(post("/admin/users/{id}/role", id)
                        .with(user("admin").roles("ADMIN")).with(csrf()).param("role", "MEMBER"))
                .andExpect(status().is3xxRedirection())
                .andExpect(flash().attribute("error", "You cannot remove your own ADMIN role"));
    }
}
