package com.fitpulse.repository;

import com.fitpulse.model.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> {
    @org.springframework.data.jpa.repository.Lock(jakarta.persistence.LockModeType.PESSIMISTIC_WRITE)
    @org.springframework.data.jpa.repository.Query("select u from User u where u.id=:id")
    Optional<User> lockForLibrary(@org.springframework.data.repository.query.Param("id") UUID id);
    Optional<User> findByGoogleSubject(String subject);
    Optional<User> findByUsername(String username);
    List<User> findTop2ByEmailIgnoreCase(String email);

    boolean existsByUsername(String username);

    @org.springframework.data.jpa.repository.Query("select count(u) > 0 from User u where lower(u.email) = lower(:email)")
    boolean existsByEmail(@org.springframework.data.repository.query.Param("email") String email);

    boolean existsByUsernameAndIdNot(String username, UUID id);

    @org.springframework.data.jpa.repository.Query("select count(u) > 0 from User u where lower(u.email) = lower(:email) and u.id <> :id")
    boolean existsByEmailAndIdNot(@org.springframework.data.repository.query.Param("email") String email,
                                  @org.springframework.data.repository.query.Param("id") UUID id);
}
