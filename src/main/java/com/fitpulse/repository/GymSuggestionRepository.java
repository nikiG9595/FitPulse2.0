package com.fitpulse.repository; import com.fitpulse.model.entity.GymSuggestion; import com.fitpulse.model.enums.ContentStatus; import org.springframework.data.jpa.repository.JpaRepository; import java.util.*;
public interface GymSuggestionRepository extends JpaRepository<GymSuggestion,Long>{
 List<GymSuggestion> findAllByOrderByCreatedAtDesc(); long countByStatus(ContentStatus status);
 @org.springframework.data.jpa.repository.Lock(jakarta.persistence.LockModeType.PESSIMISTIC_WRITE)
 @org.springframework.data.jpa.repository.Query("select s from GymSuggestion s where s.id=:id")
 Optional<GymSuggestion> lockById(@org.springframework.data.repository.query.Param("id") Long id);
}
