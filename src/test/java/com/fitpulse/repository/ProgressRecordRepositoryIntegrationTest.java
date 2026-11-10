package com.fitpulse.repository;

import com.fitpulse.model.entity.ProgressRecord;
import com.fitpulse.model.entity.User;
import com.fitpulse.model.enums.UserRole;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.dao.DataIntegrityViolationException;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest(properties = {"spring.flyway.enabled=false", "spring.jpa.hibernate.ddl-auto=create-drop"})
class ProgressRecordRepositoryIntegrationTest {
    @Autowired ProgressRecordRepository repository;
    @Autowired UserRepository userRepository;

    @Test void storesHistoryInDescendingOrderAndEnforcesOneRecordPerDay() {
        User user = new User(); user.setUsername("progress-user"); user.setEmail("progress@example.com");
        user.setPassword("encoded-password"); user.setRole(UserRole.MEMBER); userRepository.saveAndFlush(user);
        repository.saveAndFlush(record(user, LocalDate.now().minusDays(1)));
        repository.saveAndFlush(record(user, LocalDate.now()));
        assertEquals(LocalDate.now(), repository.findAllByUserOrderByRecordedAtDesc(user).getFirst().getRecordedAt());
        assertThrows(DataIntegrityViolationException.class, () -> repository.saveAndFlush(record(user, LocalDate.now())));
    }

    private ProgressRecord record(User user, LocalDate date) {
        ProgressRecord record = new ProgressRecord(); record.setUser(user); record.setWeight(new BigDecimal("80.00"));
        record.setRecordedAt(date); return record;
    }
}
