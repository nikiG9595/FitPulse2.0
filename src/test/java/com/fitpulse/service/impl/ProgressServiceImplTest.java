package com.fitpulse.service.impl;

import com.fitpulse.client.ProgressClient;
import com.fitpulse.exception.ProgressServiceException;
import com.fitpulse.model.dto.progress.ProgressCreateRequest;
import com.fitpulse.model.dto.progress.ProgressFormRequest;
import com.fitpulse.model.dto.progress.ProgressResponse;
import com.fitpulse.model.entity.User;
import com.fitpulse.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProgressServiceImplTest {
    @Mock
    private ProgressClient progressClient;

    @Mock
    private UserService userService;

    private ProgressServiceImpl progressService;
    private UUID currentUserId;

    @BeforeEach
    void setUp() {
        progressService = new ProgressServiceImpl(progressClient, userService);
        currentUserId = UUID.randomUUID();
        User user = new User();
        ReflectionTestUtils.setField(user, "id", currentUserId);
        when(userService.getCurrentUser()).thenReturn(user);
    }

    @Test
    void createAlwaysUsesAuthenticatedUserId() {
        ProgressFormRequest form = validForm();
        UUID recordId = UUID.randomUUID();
        when(progressClient.create(org.mockito.ArgumentMatchers.any())).thenReturn(
                response(recordId, currentUserId));

        progressService.create(form);

        ArgumentCaptor<ProgressCreateRequest> requestCaptor = ArgumentCaptor.forClass(ProgressCreateRequest.class);
        verify(progressClient).create(requestCaptor.capture());
        assertEquals(currentUserId, requestCaptor.getValue().userId());
    }

    @Test
    void updateRejectsRecordNotOwnedByAuthenticatedUser() {
        UUID requestedId = UUID.randomUUID();
        when(progressClient.getByUserId(currentUserId)).thenReturn(List.of(
                response(UUID.randomUUID(), currentUserId)));

        assertThrows(ProgressServiceException.class,
                () -> progressService.update(requestedId, validForm()));

        verify(progressClient, never()).update(org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.any());
    }

    @Test
    void viewingRecordRejectsRecordNotOwnedByAuthenticatedUser() {
        UUID requestedId = UUID.randomUUID();
        when(progressClient.getByUserId(currentUserId)).thenReturn(List.of(
                response(UUID.randomUUID(), currentUserId)));

        assertThrows(ProgressServiceException.class,
                () -> progressService.getCurrentUserProgress(requestedId));
    }

    @Test
    void deleteRejectsRecordNotOwnedByAuthenticatedUser() {
        UUID requestedId = UUID.randomUUID();
        when(progressClient.getByUserId(currentUserId)).thenReturn(List.of(
                response(UUID.randomUUID(), currentUserId)));

        assertThrows(ProgressServiceException.class,
                () -> progressService.delete(requestedId));

        verify(progressClient, never()).delete(org.mockito.ArgumentMatchers.any());
    }

    @Test
    void deleteCallsClientOnlyAfterOwnershipCheck() {
        UUID recordId = UUID.randomUUID();
        when(progressClient.getByUserId(currentUserId)).thenReturn(List.of(response(recordId, currentUserId)));

        progressService.delete(recordId);

        verify(progressClient).delete(recordId);
    }

    private ProgressFormRequest validForm() {
        ProgressFormRequest form = new ProgressFormRequest();
        form.setWeight(new BigDecimal("75.5"));
        form.setBodyFatPercentage(new BigDecimal("18.2"));
        form.setRecordedAt(LocalDate.now());
        form.setNote("Steady progress");
        return form;
    }

    private ProgressResponse response(UUID recordId, UUID userId) {
        return new ProgressResponse(recordId, userId, new BigDecimal("75.5"),
                new BigDecimal("18.2"), LocalDate.now(), "Steady progress",
                LocalDateTime.now(), LocalDateTime.now());
    }
}
