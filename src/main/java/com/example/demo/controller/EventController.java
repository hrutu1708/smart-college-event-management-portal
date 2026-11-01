package com.example.demo.controller;

import java.util.List;
import java.util.Map;
import com.example.demo.entity.Team;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.example.demo.entity.Event;
import com.example.demo.repository.EventRepository;
import com.example.demo.repository.RegistrationRepository;
import com.example.demo.repository.AttendanceRepository;
import com.example.demo.repository.FeedbackRepository;
import com.example.demo.repository.TeamRepository;
import com.example.demo.repository.TeamMemberRepository;
@RestController
@RequestMapping("/api/events")
@CrossOrigin(origins = "*")
public class EventController {

    @Autowired
    private EventRepository eventRepository;

    // =========================================================
    // GET ALL EVENTS
    // =========================================================

    @Autowired
    private RegistrationRepository registrationRepository;
    @Autowired
    private AttendanceRepository attendanceRepository;
    @Autowired
    private FeedbackRepository feedbackRepository;
    @Autowired
    private TeamRepository teamRepository;

    @Autowired
    private TeamMemberRepository teamMemberRepository;
    @GetMapping
    public ResponseEntity<List<Event>> getAllEvents() {

        return ResponseEntity.ok(
                eventRepository.findAll()
                
        );
    }

    // =========================================================
    // GET EVENT BY ID
    // =========================================================

    @GetMapping("/{id}")
    public ResponseEntity<?> getEvent(
            @PathVariable Long id) {

        return eventRepository.findById(id)
                .<ResponseEntity<?>>map(
                        ResponseEntity::ok
                )
                .orElseGet(
                        () -> ResponseEntity
                                .notFound()
                                .build()
                );
    }

    // =========================================================
    // CREATE EVENT
    // =========================================================

    @PostMapping
    public ResponseEntity<?> createEvent(
            @RequestBody Event event) {

        // Default event type
        if (event.getEventType() == null ||
                event.getEventType().isBlank()) {

            event.setEventType("INDIVIDUAL");
        }

        // Default competition type
        if (event.getCompetitionType() == null ||
                event.getCompetitionType().isBlank()) {

            event.setCompetitionType(
                    "INTRA_DEPARTMENT"
            );
        }

        // Group event team size
        if ("GROUP".equalsIgnoreCase(
                event.getEventType())) {

            if (event.getMaxTeamSize() == null ||
                    event.getMaxTeamSize() < 2) {

                event.setMaxTeamSize(4);
            }

        } else {

            // Individual event = team size 1
            event.setMaxTeamSize(1);
        }

        // Attendance must always start OFF
        event.setAttendanceActive(false);

        Event savedEvent =
                eventRepository.save(event);

        return ResponseEntity.ok(savedEvent);
    }

    // =========================================================
    // UPDATE EVENT
    // =========================================================

    @PutMapping("/{id}")
    public ResponseEntity<?> updateEvent(
            @PathVariable Long id,
            @RequestBody Event event) {

        return eventRepository.findById(id)
                .map(existing -> {

                    existing.setTitle(
                            event.getTitle()
                    );

                    existing.setDescription(
                            event.getDescription()
                    );

                    existing.setDate(
                            event.getDate()
                    );

                    existing.setVenue(
                            event.getVenue()
                    );

                    existing.setCapacity(
                            event.getCapacity()
                    );

                    // -------------------------
                    // EVENT TYPE
                    // -------------------------

                    if (event.getEventType() == null ||
                            event.getEventType().isBlank()) {

                        existing.setEventType(
                                "INDIVIDUAL"
                        );

                    } else {

                        existing.setEventType(
                                event.getEventType()
                        );
                    }

                    // -------------------------
                    // COMPETITION TYPE
                    // -------------------------

                    if (event.getCompetitionType() == null ||
                            event.getCompetitionType().isBlank()) {

                        existing.setCompetitionType(
                                "INTRA_DEPARTMENT"
                        );

                    } else {

                        existing.setCompetitionType(
                                event.getCompetitionType()
                        );
                    }

                    // -------------------------
                    // DEPARTMENT
                    // -------------------------

                    existing.setDepartmentName(
                            event.getDepartmentName()
                    );

                    existing.setOpponentDepartmentName(
                            event.getOpponentDepartmentName()
                    );

                    // -------------------------
                    // ORGANIZER
                    // -------------------------

                    existing.setOrganizerName(
                            event.getOrganizerName()
                    );

                    existing.setOrganizerEmail(
                            event.getOrganizerEmail()
                    );

                    existing.setOrganizerContact(
                            event.getOrganizerContact()
                    );

                    // -------------------------
                    // TEAM SIZE
                    // -------------------------

                    if ("GROUP".equalsIgnoreCase(
                            existing.getEventType())) {

                        if (event.getMaxTeamSize() == null ||
                                event.getMaxTeamSize() < 2) {

                            existing.setMaxTeamSize(4);

                        } else {

                            existing.setMaxTeamSize(
                                    event.getMaxTeamSize()
                            );
                        }

                    } else {

                        existing.setMaxTeamSize(1);
                    }

                    /*
                     * IMPORTANT:
                     * Do NOT change attendanceActive here.
                     *
                     * Attendance is controlled only by:
                     *
                     * PUT /attendance/start
                     * PUT /attendance/stop
                     */

                    Event savedEvent =
                            eventRepository.save(existing);

                    return ResponseEntity.ok(
                            savedEvent
                    );
                })
                .orElseGet(
                        () -> ResponseEntity
                                .notFound()
                                .build()
                );
    }

    // =========================================================
    // DELETE EVENT
    // =========================================================

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteEvent(@PathVariable Long id) {

        if (!eventRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }

        // Delete feedback
        feedbackRepository.deleteByEventId(id);

        // Delete attendance
        attendanceRepository.deleteByEventId(id);

        // Delete registrations
        registrationRepository.deleteByEventId(id);

        // Delete team members first
        List<Team> teams = teamRepository.findByEventId(id);

        for (Team team : teams) {
            teamMemberRepository.deleteByTeamId(team.getId());
        }

        // Delete teams
        teamRepository.deleteByEventId(id);

        // Finally delete event
        eventRepository.deleteById(id);

        return ResponseEntity.ok(
            Map.of("message", "Event deleted successfully.")
        );
    }

    // =========================================================
    // START ATTENDANCE
    // =========================================================

    @PutMapping("/{id}/attendance/start")
    public ResponseEntity<?> startAttendance(
            @PathVariable Long id) {

        return eventRepository.findById(id)
                .map(event -> {

                    event.setAttendanceActive(true);

                    Event savedEvent =
                            eventRepository.save(event);

                    return ResponseEntity.ok(
                            Map.of(
                                    "message",
                                    "Attendance started successfully.",
                                    "eventId",
                                    savedEvent.getId(),
                                    "attendanceActive",
                                    savedEvent
                                            .isAttendanceActive()
                            )
                    );
                })
                .orElseGet(
                        () -> ResponseEntity
                                .notFound()
                                .build()
                );
    }

    // =========================================================
    // STOP ATTENDANCE
    // =========================================================

    @PutMapping("/{id}/attendance/stop")
    public ResponseEntity<?> stopAttendance(
            @PathVariable Long id) {

        return eventRepository.findById(id)
                .map(event -> {

                    event.setAttendanceActive(false);

                    Event savedEvent =
                            eventRepository.save(event);

                    return ResponseEntity.ok(
                            Map.of(
                                    "message",
                                    "Attendance stopped successfully.",
                                    "eventId",
                                    savedEvent.getId(),
                                    "attendanceActive",
                                    savedEvent
                                            .isAttendanceActive()
                            )
                    );
                })
                .orElseGet(
                        () -> ResponseEntity
                                .notFound()
                                .build()
                );
    }

    // =========================================================
    // ATTENDANCE STATUS
    // =========================================================

    @GetMapping("/{id}/attendance/status")
    public ResponseEntity<?> attendanceStatus(
            @PathVariable Long id) {

        return eventRepository.findById(id)
                .map(event -> {

                    return ResponseEntity.ok(
                            Map.of(
                                    "eventId",
                                    event.getId(),
                                    "attendanceActive",
                                    event.isAttendanceActive()
                            )
                    );
                })
                .orElseGet(
                        () -> ResponseEntity
                                .notFound()
                                .build()
                );
    }
}