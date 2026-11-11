package com.fitpulse.repository; import com.fitpulse.model.entity.City; import org.springframework.data.jpa.repository.JpaRepository; import java.util.*; public interface CityRepository extends JpaRepository<City,Long>{List<City> findAllByActiveTrueOrderByName(); Optional<City> findBySlug(String slug);
 @org.springframework.data.jpa.repository.Lock(jakarta.persistence.LockModeType.PESSIMISTIC_WRITE) @org.springframework.data.jpa.repository.Query("select c from City c where c.id=:id") Optional<City> lockById(@org.springframework.data.repository.query.Param("id") Long id);
}
