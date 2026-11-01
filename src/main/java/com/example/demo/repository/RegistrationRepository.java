package com.example.demo.repository;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.demo.entity.Registration;

public interface RegistrationRepository
        extends JpaRepository<Registration, Long> {

    Optional<Registration> findByUserIdAndEventId(
            Long userId,
            Long eventId
    );

    List<Registration> findByUserId(Long userId);

    List<Registration> findByEventId(Long eventId);

    long countByEventId(Long eventId);
    @Transactional
    void deleteByEventId(Long eventId);
}