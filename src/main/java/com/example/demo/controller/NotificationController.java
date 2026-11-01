package com.example.demo.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.example.demo.entity.Notification;
import com.example.demo.repository.NotificationRepository;

@RestController
@RequestMapping("/api/notifications")
@CrossOrigin(origins = "*")
public class NotificationController {

    @Autowired
    private NotificationRepository notificationRepository;

    // =========================
    // USER NOTIFICATIONS
    // =========================

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<Notification>>
            getUserNotifications(
                    @PathVariable Long userId) {

        return ResponseEntity.ok(
                notificationRepository
                    .findByUserIdOrderByCreatedAtDesc(
                            userId
                    )
        );
    }

    // =========================
    // CREATE NOTIFICATION
    // =========================

    @PostMapping
    public ResponseEntity<Notification>
            createNotification(
                    @RequestBody Notification notification) {

        return ResponseEntity.ok(
                notificationRepository
                        .save(notification)
        );
    }

    // =========================
    // MARK AS READ
    // =========================

    @PutMapping("/{id}/read")
    public ResponseEntity<?> markAsRead(
            @PathVariable Long id) {

        return notificationRepository.findById(id)
                .map(notification -> {

                    notification.setReadStatus(true);

                    return ResponseEntity.ok(
                            notificationRepository
                                .save(notification)
                    );
                })
                .orElseGet(() ->
                        ResponseEntity.notFound().build());
    }

    // =========================
    // DELETE
    // =========================

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteNotification(
            @PathVariable Long id) {

        if (!notificationRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }

        notificationRepository.deleteById(id);

        return ResponseEntity.ok(
                "Notification deleted."
        );
    }
}