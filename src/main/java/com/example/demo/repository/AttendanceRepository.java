package com.example.demo.repository;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.demo.entity.Attendance;

public interface AttendanceRepository extends JpaRepository<Attendance, Long> {

    Optional<Attendance> findByUserIdAndEventId(
            Long userId,
            Long eventId
    );

    List<Attendance> findByUserId(Long userId);

    List<Attendance> findByEventId(Long eventId);
    @Transactional
    void deleteByEventId(Long eventId);
}