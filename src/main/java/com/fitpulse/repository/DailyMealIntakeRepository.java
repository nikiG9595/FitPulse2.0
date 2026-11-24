package com.fitpulse.repository;

import com.fitpulse.model.entity.DailyMealIntake;
import com.fitpulse.model.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface DailyMealIntakeRepository extends JpaRepository<DailyMealIntake, Long> {
    List<DailyMealIntake> findAllByUserAndPlanIdAndIntakeDate(User user, Long planId, LocalDate intakeDate);
}
