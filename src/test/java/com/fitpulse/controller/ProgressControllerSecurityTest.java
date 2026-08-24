package com.fitpulse.controller;

import com.fitpulse.config.SecurityConfig;
import com.fitpulse.exception.ProgressServiceException;
import com.fitpulse.model.dto.progress.ProgressResponse;
import com.fitpulse.service.ProgressService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrlPattern;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@WebMvcTest(ProgressController.class)
@Import(SecurityConfig.class)
class ProgressControllerSecurityTest {
    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ProgressService progressService;

    @Test
    void unauthenticatedUserIsRedirectedToLogin() throws Exception {
        mockMvc.perform(get("/progress"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("**/login"));
    }

    @Test
    void memberCanViewProgress() throws Exception {
        when(progressService.getCurrentUserProgress()).thenReturn(List.of());

        mockMvc.perform(get("/progress").with(user("member").roles("MEMBER")))
                .andExpect(status().isOk());
    }

    @Test
    void adminCanViewProgress() throws Exception {
        when(progressService.getCurrentUserProgress()).thenReturn(List.of());

        mockMvc.perform(get("/progress").with(user("admin").roles("ADMIN")))
                .andExpect(status().isOk());
    }

    @Test
    void adminCanCreateEditAndDeleteOwnProgressWithCsrfToken() throws Exception {
        UUID id = UUID.randomUUID();
        when(progressService.getCurrentUserProgress(id)).thenReturn(new ProgressResponse(
                id, UUID.randomUUID(), new BigDecimal("75.5"), new BigDecimal("18.2"),
                LocalDate.of(2020, 8, 20), "Steady improvement", null, null));

        mockMvc.perform(post("/progress/create").with(user("admin").roles("ADMIN")).with(csrf())
                        .param("weight", "75.5").param("recordedAt", "2020-08-20"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/progress"));
        mockMvc.perform(get("/progress/{id}/edit", id).with(user("admin").roles("ADMIN")))
                .andExpect(status().isOk());
        mockMvc.perform(post("/progress/{id}/edit", id).with(user("admin").roles("ADMIN")).with(csrf())
                        .param("weight", "75.5").param("recordedAt", "2020-08-20"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/progress"));
        mockMvc.perform(post("/progress/{id}/delete", id)
                        .with(user("admin").roles("ADMIN")).with(csrf()))
                .andExpect(status().is3xxRedirection());

        verify(progressService).create(org.mockito.ArgumentMatchers.any());
        verify(progressService).update(org.mockito.ArgumentMatchers.eq(id), org.mockito.ArgumentMatchers.any());
        verify(progressService).delete(id);
    }

    @Test
    void createRequiresCsrfToken() throws Exception {
        mockMvc.perform(post("/progress/create")
                        .with(user("member").roles("MEMBER"))
                        .param("weight", "75.5")
                        .param("recordedAt", "2025-01-01"))
                .andExpect(status().isForbidden());
    }

    @Test
    void memberCanDeleteOwnRecordWithCsrfToken() throws Exception {
        UUID id = UUID.randomUUID();

        mockMvc.perform(post("/progress/{id}/delete", id)
                        .with(user("member").roles("MEMBER"))
                        .with(csrf()))
                .andExpect(status().is3xxRedirection());

        verify(progressService).delete(id);
    }

    @Test
    void createFormDefaultsRecordedDateAndValidSubmissionRedirects() throws Exception {
        mockMvc.perform(get("/progress/create").with(user("member").roles("MEMBER")))
                .andExpect(status().isOk()).andExpect(model().attributeExists("progressFormRequest"));

        mockMvc.perform(post("/progress/create").with(user("member").roles("MEMBER")).with(csrf())
                        .param("weight", "75.5").param("bodyFatPercentage", "18.2")
                        .param("recordedAt", "2020-08-20").param("note", "Steady improvement"))
                .andExpect(status().is3xxRedirection()).andExpect(redirectedUrl("/progress"));
        verify(progressService).create(org.mockito.ArgumentMatchers.any());
    }

    @Test
    void progressServiceFailureIsShownOnCreateForm() throws Exception {
        doThrow(new ProgressServiceException("Progress service unavailable"))
                .when(progressService).create(org.mockito.ArgumentMatchers.any());
        mockMvc.perform(post("/progress/create").with(user("member").roles("MEMBER")).with(csrf())
                        .param("weight", "75.5").param("recordedAt", "2020-08-20"))
                .andExpect(status().isOk()).andExpect(view().name("progress/form"))
                .andExpect(model().attribute("globalError", "Progress service unavailable"));
    }

    @Test
    void editLoadsOwnedRecordAndUpdateFailurePreservesId() throws Exception {
        UUID id = UUID.randomUUID();
        when(progressService.getCurrentUserProgress(id)).thenReturn(new ProgressResponse(
                id, UUID.randomUUID(), new BigDecimal("75.5"), new BigDecimal("18.2"),
                LocalDate.of(2020, 8, 20), "Steady improvement", null, null));
        mockMvc.perform(get("/progress/{id}/edit", id).with(user("member").roles("MEMBER")))
                .andExpect(status().isOk()).andExpect(model().attribute("progressId", id));

        doThrow(new ProgressServiceException("Update rejected")).when(progressService)
                .update(org.mockito.ArgumentMatchers.eq(id), org.mockito.ArgumentMatchers.any());
        mockMvc.perform(post("/progress/{id}/edit", id).with(user("member").roles("MEMBER")).with(csrf())
                        .param("weight", "75.5").param("recordedAt", "2020-08-20"))
                .andExpect(status().isOk()).andExpect(model().attribute("progressId", id))
                .andExpect(model().attribute("globalError", "Update rejected"));
    }

    @Test
    void deleteFailureBecomesFlashError() throws Exception {
        UUID id = UUID.randomUUID();
        doThrow(new ProgressServiceException("Delete rejected")).when(progressService).delete(id);
        mockMvc.perform(post("/progress/{id}/delete", id).with(user("member").roles("MEMBER")).with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.flash()
                        .attribute("error", "Delete rejected"));
    }

    @Test
    void invalidCreateSubmissionStaysOnFormWithoutCallingService() throws Exception {
        mockMvc.perform(post("/progress/create").with(user("member").roles("MEMBER")).with(csrf())
                        .param("weight", "10").param("recordedAt", "2999-01-01"))
                .andExpect(status().isOk()).andExpect(view().name("progress/form"))
                .andExpect(model().attributeHasFieldErrors("progressFormRequest", "weight", "recordedAt"));
        org.mockito.Mockito.verify(progressService, org.mockito.Mockito.never())
                .create(org.mockito.ArgumentMatchers.any());
    }

    @Test
    void invalidEditPreservesRecordIdAndSuccessfulEditRedirects() throws Exception {
        UUID id = UUID.randomUUID();
        mockMvc.perform(post("/progress/{id}/edit", id).with(user("member").roles("MEMBER")).with(csrf())
                        .param("weight", "10").param("recordedAt", "2020-01-01"))
                .andExpect(status().isOk()).andExpect(view().name("progress/form"))
                .andExpect(model().attribute("progressId", id));

        mockMvc.perform(post("/progress/{id}/edit", id).with(user("member").roles("MEMBER")).with(csrf())
                        .param("weight", "75.5").param("recordedAt", "2020-01-01"))
                .andExpect(status().is3xxRedirection()).andExpect(redirectedUrl("/progress"))
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.flash()
                        .attribute("success", "Progress record updated successfully."));
        verify(progressService).update(org.mockito.ArgumentMatchers.eq(id), org.mockito.ArgumentMatchers.any());
    }
}
