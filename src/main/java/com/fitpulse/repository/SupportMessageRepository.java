package com.fitpulse.repository;

import com.fitpulse.model.entity.SupportMessage;
import com.fitpulse.model.enums.*;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.repository.*;

public interface SupportMessageRepository extends JpaRepository<SupportMessage,Long> {
    long countByStatus(SupportStatus status);
    @Lock(jakarta.persistence.LockModeType.PESSIMISTIC_WRITE)
    @Query("select m from SupportMessage m where m.id=:id")
    java.util.Optional<SupportMessage> lockById(@org.springframework.data.repository.query.Param("id") Long id);
    Page<SupportMessage> findByCategory(SupportCategory category,Pageable page);
    Page<SupportMessage> findByStatus(SupportStatus status,Pageable page);
    Page<SupportMessage> findByCategoryAndStatus(SupportCategory category,SupportStatus status,Pageable page);
}
