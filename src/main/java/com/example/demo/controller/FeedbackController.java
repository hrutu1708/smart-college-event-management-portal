package com.example.demo.controller;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.example.demo.entity.Feedback;
import com.example.demo.repository.FeedbackRepository;

@RestController
@RequestMapping("/api/feedback")
@CrossOrigin(origins = "*")
public class FeedbackController {

    @Autowired
    private FeedbackRepository feedbackRepository;


    // Submit feedback
    @PostMapping
    public ResponseEntity<?> submitFeedback(
            @RequestBody Feedback feedback) {

        if (feedback.getRating() == null ||
                feedback.getRating() < 1 ||
                feedback.getRating() > 5) {

            return ResponseEntity.badRequest()
                    .body("Rating must be between 1 and 5.");
        }

        Optional<Feedback> existing =
                feedbackRepository.findByUserIdAndEventId(
                        feedback.getUserId(),
                        feedback.getEventId()
                );

        if (existing.isPresent()) {
            return ResponseEntity.badRequest()
                    .body("You have already submitted feedback for this event.");
        }

        feedback.setSubmittedAt(LocalDateTime.now());

        Feedback saved =
                feedbackRepository.save(feedback);

        return ResponseEntity.ok(saved);
    }


    // Get all feedback
    @GetMapping
    public ResponseEntity<List<Feedback>> getAllFeedback() {

        return ResponseEntity.ok(
                feedbackRepository.findAll()
        );
    }


    // Get feedback by ID
    @GetMapping("/{id}")
    public ResponseEntity<?> getFeedbackById(
            @PathVariable Long id) {

        Optional<Feedback> feedback =
                feedbackRepository.findById(id);

        if (feedback.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(feedback.get());
    }


    // Get feedback for an event
    @GetMapping("/event/{eventId}")
    public ResponseEntity<List<Feedback>> getEventFeedback(
            @PathVariable Long eventId) {

        return ResponseEntity.ok(
                feedbackRepository.findByEventId(eventId)
        );
    }


    // Get feedback by student
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<Feedback>> getUserFeedback(
            @PathVariable Long userId) {

        return ResponseEntity.ok(
                feedbackRepository.findByUserId(userId)
        );
    }
}