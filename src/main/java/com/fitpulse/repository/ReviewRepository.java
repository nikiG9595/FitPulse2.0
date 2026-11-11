package com.fitpulse.repository; import com.fitpulse.model.entity.*; import com.fitpulse.model.enums.ContentStatus; import org.springframework.data.jpa.repository.*; import java.util.*;
public interface ReviewRepository extends JpaRepository<Review,Long>{
 List<Review> findAllByGymAndStatusOrderByCreatedAtDesc(Gym gym,ContentStatus status); Optional<Review> findByGymAndUser(Gym gym,User user);
 @Lock(jakarta.persistence.LockModeType.PESSIMISTIC_WRITE) @Query("select r from Review r where r.gym=:gym and r.user=:user") Optional<Review> lockOwned(@org.springframework.data.repository.query.Param("gym") Gym gym,@org.springframework.data.repository.query.Param("user") User user);
 @Lock(jakarta.persistence.LockModeType.PESSIMISTIC_WRITE) @Query("select r from Review r where r.id=:id") Optional<Review> lockById(@org.springframework.data.repository.query.Param("id") Long id);
 long countByStatus(ContentStatus status); @Query("select coalesce(avg(r.rating),0) from Review r where r.gym=:gym and r.status=com.fitpulse.model.enums.ContentStatus.PUBLISHED") double averageForGym(Gym gym);
}
