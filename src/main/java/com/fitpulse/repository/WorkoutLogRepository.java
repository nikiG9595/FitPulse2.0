package com.fitpulse.repository;
import com.fitpulse.model.entity.*;
import org.springframework.data.jpa.repository.JpaRepository;
public interface WorkoutLogRepository extends JpaRepository<WorkoutLog,Long> { java.util.List<WorkoutLog> findAllByUserOrderByPerformedAtDesc(User user); java.util.Optional<WorkoutLog> findByIdAndUser(Long id,User user); }
