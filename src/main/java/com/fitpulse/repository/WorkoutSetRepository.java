package com.fitpulse.repository;
import com.fitpulse.model.entity.*;
import org.springframework.data.jpa.repository.JpaRepository;
public interface WorkoutSetRepository extends JpaRepository<WorkoutSet,Long> { java.util.List<WorkoutSet> findAllByWorkoutLogOrderById(WorkoutLog log); java.util.List<WorkoutSet> findAllByWorkoutLog_UserOrderByWorkoutLog_PerformedAtAsc(User user); java.util.Optional<WorkoutSet> findByIdAndWorkoutLog(Long id,WorkoutLog log); }
