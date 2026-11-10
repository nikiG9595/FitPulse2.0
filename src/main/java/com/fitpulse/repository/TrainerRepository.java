package com.fitpulse.repository;
import com.fitpulse.model.entity.*;
import org.springframework.data.jpa.repository.JpaRepository;
public interface TrainerRepository extends JpaRepository<Trainer,Long> { java.util.Optional<Trainer> findFirstByName(String name); java.util.List<Trainer> findAllByPublishedTrueOrderByName(); java.util.List<Trainer> findAllByOrderByName(); java.util.Optional<Trainer> findByIdAndPublishedTrue(Long id); }
