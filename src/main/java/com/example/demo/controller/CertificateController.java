package com.example.demo.controller;

import java.io.ByteArrayOutputStream;
import java.util.List;
import java.util.Optional;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.example.demo.entity.Attendance;
import com.example.demo.entity.Event;
import com.example.demo.entity.Registration;
import com.example.demo.entity.Team;
import com.example.demo.entity.TeamMember;
import com.example.demo.entity.User;

import com.example.demo.repository.AttendanceRepository;
import com.example.demo.repository.EventRepository;
import com.example.demo.repository.RegistrationRepository;
import com.example.demo.repository.TeamMemberRepository;
import com.example.demo.repository.TeamRepository;
import com.example.demo.repository.UserRepository;

import com.example.demo.service.CertificateService;

@RestController
@RequestMapping("/api/certificates")
@CrossOrigin(origins = "*")
public class CertificateController {

    @Autowired
    private CertificateService certificateService;

    @Autowired
    private AttendanceRepository attendanceRepository;

    @Autowired
    private RegistrationRepository registrationRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private TeamRepository teamRepository;

    @Autowired
    private TeamMemberRepository teamMemberRepository;


    // =====================================================
    // INDIVIDUAL CERTIFICATE
    // =====================================================

    @GetMapping("/{registrationId}")
    public ResponseEntity<?> downloadCertificate(
            @PathVariable Long registrationId) {

        Optional<Registration> registrationOptional =
                registrationRepository.findById(
                        registrationId
                );

        if (registrationOptional.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Registration registration =
                registrationOptional.get();

        Optional<Attendance> attendanceOptional =
                attendanceRepository
                    .findByUserIdAndEventId(
                        registration.getUserId(),
                        registration.getEventId()
                    );

        if (attendanceOptional.isEmpty()) {

            return ResponseEntity.badRequest()
                    .body(
                        "Certificate available only "
                        + "after attendance."
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

        User user = userOptional.get();
        Event event = eventOptional.get();

        byte[] pdf =
                certificateService.generateCertificate(
                    user.getName(),
                    event.getTitle(),
                    event.getDate().toString()
                );

        return ResponseEntity.ok()
                .header(
                    HttpHeaders.CONTENT_DISPOSITION,
                    "attachment; filename=certificate-"
                    + registrationId
                    + ".pdf"
                )
                .contentType(
                    MediaType.APPLICATION_PDF
                )
                .body(pdf);
    }


    // =====================================================
    // TEAM CERTIFICATES
    // =====================================================

    @GetMapping("/team/{teamId}")
    public ResponseEntity<?> downloadTeamCertificates(
            @PathVariable Long teamId) {

        Optional<Team> teamOptional =
                teamRepository.findById(teamId);

        if (teamOptional.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Team team = teamOptional.get();

        Optional<Event> eventOptional =
                eventRepository.findById(
                        team.getEventId()
                );

        if (eventOptional.isEmpty()) {
            return ResponseEntity.badRequest()
                    .body("Event not found.");
        }

        Event event = eventOptional.get();

        List<TeamMember> members =
                teamMemberRepository
                    .findByTeamId(teamId);

        if (members.isEmpty()) {

            return ResponseEntity.badRequest()
                    .body(
                        "No members found in this team."
                    );
        }

        try {

            ByteArrayOutputStream zipOutput =
                    new ByteArrayOutputStream();

            ZipOutputStream zip =
                    new ZipOutputStream(zipOutput);

            int certificateCount = 0;

            for (TeamMember member : members) {

                Optional<Attendance> attendance =
                        attendanceRepository
                            .findByUserIdAndEventId(
                                member.getUserId(),
                                team.getEventId()
                            );

                // Only members who attended
                // receive certificates
                if (attendance.isEmpty()) {
                    continue;
                }

                Optional<User> userOptional =
                        userRepository.findById(
                                member.getUserId()
                        );

                if (userOptional.isEmpty()) {
                    continue;
                }

                User user = userOptional.get();

                byte[] pdf =
                        certificateService
                            .generateCertificate(
                                user.getName(),
                                event.getTitle(),
                                event.getDate().toString()
                            );

                String safeName =
                        user.getName()
                            .replaceAll(
                                "[^a-zA-Z0-9_-]",
                                "_"
                            );

                ZipEntry entry =
                        new ZipEntry(
                            safeName
                            + "-certificate.pdf"
                        );

                zip.putNextEntry(entry);
                zip.write(pdf);
                zip.closeEntry();

                certificateCount++;
            }

            zip.close();

            if (certificateCount == 0) {

                return ResponseEntity.badRequest()
                        .body(
                            "No team member has attended "
                            + "this event yet."
                        );
            }

            return ResponseEntity.ok()
                    .header(
                        HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=team-"
                        + teamId
                        + "-certificates.zip"
                    )
                    .contentType(
                        MediaType.APPLICATION_OCTET_STREAM
                    )
                    .body(zipOutput.toByteArray());

        } catch (Exception e) {

            return ResponseEntity
                    .internalServerError()
                    .body(
                        "Team certificate generation failed: "
                        + e.getMessage()
                    );
        }
    }
}