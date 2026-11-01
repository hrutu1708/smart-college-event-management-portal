package com.example.demo.controller;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.example.demo.entity.Attendance;
import com.example.demo.entity.Event;
import com.example.demo.entity.Registration;
import com.example.demo.entity.Team;
import com.example.demo.entity.TeamMember;

import com.example.demo.repository.AttendanceRepository;
import com.example.demo.repository.EventRepository;
import com.example.demo.repository.RegistrationRepository;
import com.example.demo.repository.TeamMemberRepository;
import com.example.demo.repository.TeamRepository;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;

@RestController
@RequestMapping("/api/qr")
@CrossOrigin(origins = "*")
public class QrController {

    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private RegistrationRepository registrationRepository;

    @Autowired
    private AttendanceRepository attendanceRepository;

    @Autowired
    private TeamRepository teamRepository;

    @Autowired
    private TeamMemberRepository teamMemberRepository;

    // =====================================================
    // GENERATE ATTENDANCE QR
    // =====================================================

    @GetMapping(
        value = "/attendance/{eventId}",
        produces = MediaType.IMAGE_PNG_VALUE
    )
    public ResponseEntity<byte[]> generateAttendanceQr(
            @PathVariable Long eventId) {

        Optional<Event> optionalEvent =
                eventRepository.findById(eventId);

        if (optionalEvent.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Event event = optionalEvent.get();

        if (!Boolean.TRUE.equals(
                event.isAttendanceActive())) {

            return ResponseEntity.badRequest()
                    .body(
                        "Attendance is not active."
                            .getBytes(StandardCharsets.UTF_8)
                    );
        }

        try {

            String qrData =
                    "ATTENDANCE_EVENT_" + eventId;

            BitMatrix matrix =
                    new MultiFormatWriter().encode(
                            qrData,
                            BarcodeFormat.QR_CODE,
                            350,
                            350
                    );

            ByteArrayOutputStream output =
                    new ByteArrayOutputStream();

            MatrixToImageWriter.writeToStream(
                    matrix,
                    "PNG",
                    output
            );

            return ResponseEntity.ok()
                    .contentType(MediaType.IMAGE_PNG)
                    .body(output.toByteArray());

        } catch (Exception e) {

            return ResponseEntity
                    .internalServerError()
                    .body(
                        ("QR generation failed: "
                        + e.getMessage())
                        .getBytes(StandardCharsets.UTF_8)
                    );
        }
    }

    // =====================================================
    // SCAN ATTENDANCE QR
    // =====================================================

    @PostMapping("/scan")
    public ResponseEntity<?> scanAttendance(
            @RequestBody Map<String, Object> request) {

        try {

            if (request.get("userId") == null ||
                    request.get("qrData") == null) {

                return ResponseEntity.badRequest()
                        .body(
                            "User ID and QR data are required."
                        );
            }

            Long userId =
                    Long.valueOf(
                        request.get("userId").toString()
                    );

            String qrData =
                    request.get("qrData").toString();

            // ---------------------------------------------
            // Validate QR format
            // ---------------------------------------------

            if (!qrData.startsWith(
                    "ATTENDANCE_EVENT_")) {

                return ResponseEntity.badRequest()
                        .body(
                            "Invalid attendance QR code."
                        );
            }

            String eventIdText =
                    qrData.replace(
                        "ATTENDANCE_EVENT_",
                        ""
                    );

            Long eventId =
                    Long.valueOf(eventIdText);

            // ---------------------------------------------
            // Find event
            // ---------------------------------------------

            Optional<Event> eventOptional =
                    eventRepository.findById(eventId);

            if (eventOptional.isEmpty()) {

                return ResponseEntity.badRequest()
                        .body("Event not found.");
            }

            Event event = eventOptional.get();

            // ---------------------------------------------
            // Attendance must be active
            // ---------------------------------------------

            if (!Boolean.TRUE.equals(
                    event.isAttendanceActive())) {

                return ResponseEntity.badRequest()
                        .body(
                            "Attendance is not currently active."
                        );
            }

            // ---------------------------------------------
            // Check whether student is eligible
            // ---------------------------------------------

            boolean eligible = false;

            // Individual registration
            Optional<Registration> registration =
                    registrationRepository
                        .findByUserIdAndEventId(
                                userId,
                                eventId
                        );

            if (registration.isPresent()) {
                eligible = true;
            }

            // Group event → check team membership
            if (!eligible &&
                    "GROUP".equalsIgnoreCase(
                            event.getEventType())) {

                List<Team> teams =
                        teamRepository
                            .findByEventId(eventId);

                for (Team team : teams) {

                    Optional<TeamMember> member =
                            teamMemberRepository
                                .findByTeamIdAndUserId(
                                    team.getId(),
                                    userId
                                );

                    if (member.isPresent()) {
                        eligible = true;
                        break;
                    }
                }
            }

            if (!eligible) {

                return ResponseEntity.badRequest()
                        .body(
                            "You are not registered "
                            + "or not a member of a team "
                            + "for this event."
                        );
            }

            // ---------------------------------------------
            // Prevent duplicate attendance
            // ---------------------------------------------

            Optional<Attendance> existing =
                    attendanceRepository
                        .findByUserIdAndEventId(
                                userId,
                                eventId
                        );

            if (existing.isPresent()) {

                return ResponseEntity.badRequest()
                        .body(
                            "Attendance already marked."
                        );
            }

            // ---------------------------------------------
            // Save attendance
            // ---------------------------------------------

            Attendance attendance =
                    new Attendance();

            attendance.setUserId(userId);
            attendance.setEventId(eventId);
            attendance.setStatus("PRESENT");
            attendance.setMarkedAt(
                    LocalDateTime.now()
            );

            Attendance saved =
                    attendanceRepository.save(
                            attendance
                    );

            // ---------------------------------------------
            // Response
            // ---------------------------------------------

            Map<String, Object> response =
                    new HashMap<>();

            response.put(
                    "message",
                    "Attendance marked successfully."
            );

            response.put(
                    "attendanceId",
                    saved.getId()
            );

            response.put(
                    "eventId",
                    eventId
            );

            response.put(
                    "userId",
                    userId
            );

            response.put(
                    "status",
                    "PRESENT"
            );

            response.put(
                    "certificateEligible",
                    true
            );

            return ResponseEntity.ok(response);

        } catch (Exception e) {

            return ResponseEntity.badRequest()
                    .body(
                        "Invalid QR attendance request."
                    );
        }
    }
}