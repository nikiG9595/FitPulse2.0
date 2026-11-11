package com.fitpulse.repository;
import com.fitpulse.model.entity.*;import org.springframework.data.jpa.repository.*;import org.springframework.data.repository.query.Param;import jakarta.persistence.LockModeType;import java.util.*;
public interface WorkoutSessionRepository extends JpaRepository<WorkoutSession,Long>{
 Optional<WorkoutSession> findByIdAndUser(Long id,User user);
 Optional<WorkoutSession> findByActiveKeyAndUser(String key,User user);
 List<WorkoutSession> findTop30ByUserOrderByCreatedAtDesc(User user);
 List<WorkoutSession> findAllByUserAndStatusOrderByCreatedAtDesc(User user,String status);
 @Lock(LockModeType.PESSIMISTIC_WRITE) @Query("select s from WorkoutSession s where s.program=:program and s.user=:user order by s.id") List<WorkoutSession> lockForProgram(@Param("program")UserProgram program,@Param("user")User user);
 @Lock(LockModeType.PESSIMISTIC_WRITE) @Query("select s from WorkoutSession s where s.id=:id and s.user=:user") Optional<WorkoutSession> lockOwned(@Param("id")Long id,@Param("user")User user);
}
