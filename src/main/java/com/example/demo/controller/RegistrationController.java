package com.example.demo.controller;
import com.example.demo.service.EmailService;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.example.demo.entity.Event;
import com.example.demo.entity.Notification;
import com.example.demo.entity.Registration;
import com.example.demo.entity.User;

import com.example.demo.repository.EventRepository;
import com.example.demo.repository.NotificationRepository;
import com.example.demo.repository.RegistrationRepository;
import com.example.demo.repository.UserRepository;

@RestController
@RequestMapping("/api/registrations")
@CrossOrigin(origins = "*")
public class RegistrationController {

    @Autowired
    private RegistrationRepository registrationRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private NotificationRepository notificationRepository;
    
    @Autowired
    private EmailService emailService;


    // =====================================================
    // REGISTER
    // =====================================================

    @PostMapping
    public ResponseEntity<?> register(
            @RequestBody Registration registration) {

        if (registration.getUserId() == null ||
                registration.getEventId() == null) {

            return ResponseEntity.badRequest()
                    .body(
                        "User ID and Event ID are required."
                    );
        }

        Optional<Registration> existing =
                registrationRepository
                    .findByUserIdAndEventId(
                        registration.getUserId(),
                        registration.getEventId()
                    );

        if (existing.isPresent()) {

            return ResponseEntity.badRequest()
                    .body(
                        "You are already registered "
                        + "for this event."
                    );
        }

        Optional<User> userOptional =
                userRepository.findById(
                        registration.getUserId()
                );

        if (userOptional.isEmpty()) {
            return ResponseEntity.badRequest()
                    .body("Student not found.");
        }

        Optional<Event> eventOptional =
                eventRepository.findById(
                        registration.getEventId()
                );

        if (eventOptional.isEmpty()) {
            return ResponseEntity.badRequest()
                    .body("Event not found.");
        }

        registration.setStatus("CONFIRMED");
        registration.setRegisteredAt(
                LocalDateTime.now()
        );

        Registration saved =
                registrationRepository.save(
                        registration
                );

        User user = userOptional.get();
        Event event = eventOptional.get();

        // =================================================
        // CREATE NOTIFICATION
        // =================================================

        Notification notification =
                new Notification();

        notification.setUserId(
                user.getId()
        );

        notification.setTitle(
                "Registration Confirmed"
        );

        notification.setMessage(
                "You have successfully registered "
                + "for " + event.getTitle()
                + "."
        );

        notification.setType(
                "REGISTRATION"
        );

        notification.setReadStatus(false);

        notificationRepository.save(
                notification
        );
        emailService.sendRegistrationConfirmation(
                user.getName(),
                user.getEmail(),
                event.getTitle(),
                event.getDate().toString(),
                event.getVenue()
        );

        return ResponseEntity.ok(
                buildDetailedResponse(saved)
        );
    }


    // =====================================================
    // GET ALL REGISTRATIONS
    // =====================================================

    @GetMapping
    public ResponseEntity<List<Map<String, Object>>>
            getAllRegistrations() {

        List<Registration> registrations =
                registrationRepository.findAll();

        List<Map<String, Object>> response =
                new ArrayList<>();

        for (Registration registration :
                registrations) {

            response.add(
                buildDetailedResponse(registration)
            );
        }

        return ResponseEntity.ok(response);
    }


    // =====================================================
    // GET BY ID
    // =====================================================

    @GetMapping("/{id}")
    public ResponseEntity<?> getRegistration(
            @PathVariable Long id) {

        Optional<Registration> optional =
                registrationRepository.findById(id);

        if (optional.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(
                buildDetailedResponse(
                    optional.get()
                )
        );
    }


    // =====================================================
    // DELETE
    // =====================================================

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteRegistration(
            @PathVariable Long id) {

        if (!registrationRepository.existsById(id)) {

            return ResponseEntity.notFound()
                    .build();
        }

        registrationRepository.deleteById(id);

        return ResponseEntity.ok(
                "Registration cancelled successfully."
        );
    }


    // =====================================================
    // DETAILED RESPONSE
    // =====================================================

    private Map<String, Object>
            buildDetailedResponse(
                    Registration registration) {

        Map<String, Object> map =
                new HashMap<>();

        map.put(
                "registrationId",
                registration.getId()
        );

        map.put(
                "status",
                registration.getStatus()
        );

        map.put(
                "userId",
                registration.getUserId()
        );

        map.put(
                "eventId",
                registration.getEventId()
        );

        Optional<User> user =
                userRepository.findById(
                        registration.getUserId()
                );

        Optional<Event> event =
                eventRepository.findById(
                        registration.getEventId()
                );

        if (user.isPresent()) {

            map.put(
                    "studentName",
                    user.get().getName()
            );

            map.put(
                    "studentEmail",
                    user.get().getEmail()
            );

            map.put(
                    "studentDepartment",
                    user.get().getDepartment()
            );
        }

        if (event.isPresent()) {

            map.put(
                    "eventTitle",
                    event.get().getTitle()
            );

            map.put(
                    "eventDate",
                    event.get().getDate()
            );

            map.put(
                    "eventVenue",
                    event.get().getVenue()
            );

            map.put(
                    "eventDescription",
                    event.get().getDescription()
            );

            map.put(
                    "eventType",
                    event.get().getEventType()
            );

            map.put(
                    "competitionType",
                    event.get().getCompetitionType()
            );
        }

        return map;
    }
}