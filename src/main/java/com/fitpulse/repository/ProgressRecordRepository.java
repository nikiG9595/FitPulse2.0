package com.fitpulse.repository;

import com.fitpulse.model.entity.ProgressRecord;
import com.fitpulse.model.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ProgressRecordRepository extends JpaRepository<ProgressRecord, UUID> {
    List<ProgressRecord> findAllByUserOrderByRecordedAtDesc(User user);
    Optional<ProgressRecord> findByIdAndUser(UUID id, User user);
    boolean existsByUserAndRecordedAt(User user, LocalDate recordedAt);
    boolean existsByUserAndRecordedAtAndIdNot(User user, LocalDate recordedAt, UUID id);
}
