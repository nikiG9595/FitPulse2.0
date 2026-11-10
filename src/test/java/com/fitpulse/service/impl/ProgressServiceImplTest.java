package com.fitpulse.service.impl;

import com.fitpulse.exception.ProgressServiceException;
import com.fitpulse.model.dto.progress.ProgressFormRequest;
import com.fitpulse.model.entity.ProgressRecord;
import com.fitpulse.model.entity.User;
import com.fitpulse.repository.ProgressRecordRepository;
import com.fitpulse.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProgressServiceImplTest {
    @Mock ProgressRecordRepository repository;
    @Mock UserService userService;
    private ProgressServiceImpl service;
    private User user;

    @BeforeEach
    void setUp() {
        service = new ProgressServiceImpl(repository, userService);
        user = new User();
        org.springframework.test.util.ReflectionTestUtils.setField(user, "id", UUID.randomUUID());
        when(userService.getCurrentUser()).thenReturn(user);
    }

    @Test void createPersistsOwnedNormalizedRecord() {
        ProgressFormRequest request = request(LocalDate.now()); service.create(request);
        var captor = org.mockito.ArgumentCaptor.forClass(ProgressRecord.class);
        verify(repository).save(captor.capture()); assertSame(user, captor.getValue().getUser());
        assertEquals("note", captor.getValue().getNote());
    }

    @Test void duplicateDateIsRejected() {
        ProgressFormRequest request = request(LocalDate.now());
        when(repository.existsByUserAndRecordedAt(user, request.getRecordedAt())).thenReturn(true);
        assertThrows(ProgressServiceException.class, () -> service.create(request)); verify(repository, never()).save(any());
    }

    @Test void historyIsOwnedAndSortedByRepository() {
        ProgressRecord record = record(); when(repository.findAllByUserOrderByRecordedAtDesc(user)).thenReturn(List.of(record));
        var result = service.getCurrentUserProgress(); assertEquals(1, result.size()); assertEquals(user.getId(), result.getFirst().userId());
    }

    @Test void updateAndDeleteRequireOwnedRecord() {
        UUID id = UUID.randomUUID(); when(repository.findByIdAndUser(id, user)).thenReturn(Optional.empty());
        assertThrows(ProgressServiceException.class, () -> service.update(id, request(LocalDate.now())));
        assertThrows(ProgressServiceException.class, () -> service.delete(id));
    }

    @Test void ownerCanUpdateAndDelete() {
        UUID id = UUID.randomUUID(); ProgressRecord record = record();
        when(repository.findByIdAndUser(id, user)).thenReturn(Optional.of(record));
        service.update(id, request(LocalDate.now().minusDays(1))); verify(repository).save(record);
        service.delete(id); verify(repository).delete(record);
    }

    private ProgressRecord record() {
        ProgressRecord record = new ProgressRecord(); record.setUser(user); record.setWeight(new BigDecimal("80.00"));
        record.setRecordedAt(LocalDate.now()); return record;
    }

    private ProgressFormRequest request(LocalDate date) {
        ProgressFormRequest request = new ProgressFormRequest(); request.setWeight(new BigDecimal("80.00"));
        request.setRecordedAt(date); request.setNote("  note  "); return request;
    }
}
