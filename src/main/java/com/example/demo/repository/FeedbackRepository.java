package com.example.demo.repository;

import java.util.List;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.demo.entity.Feedback;
import org.springframework.transaction.annotation.Transactional;
public interface FeedbackRepository extends JpaRepository<Feedback, Long> {

    Optional<Feedback> findByUserIdAndEventId(
            Long userId,
            Long eventId
    );

    List<Feedback> findByEventId(Long eventId);

    List<Feedback> findByUserId(Long userId);
    @Transactional
    void deleteByEventId(Long eventId);
}