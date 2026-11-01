package com.example.demo.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.example.demo.entity.Event;
import com.example.demo.entity.Team;
import com.example.demo.entity.TeamMember;
import com.example.demo.entity.User;
import com.example.demo.repository.EventRepository;
import com.example.demo.repository.TeamMemberRepository;
import com.example.demo.repository.TeamRepository;
import com.example.demo.repository.UserRepository;

@RestController
@RequestMapping("/api/team-members")
@CrossOrigin(origins = "*")
public class TeamMemberController {

    @Autowired
    private TeamMemberRepository teamMemberRepository;

    @Autowired
    private TeamRepository teamRepository;

    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private UserRepository userRepository;

    @PostMapping
    public ResponseEntity<?> addMember(
            @RequestBody TeamMember member) {

        if (member.getTeamId() == null ||
                member.getUserId() == null) {

            return ResponseEntity.badRequest()
                    .body("Team ID and User ID are required.");
        }

        Team team = teamRepository
                .findById(member.getTeamId())
                .orElse(null);

        if (team == null) {
            return ResponseEntity.badRequest()
                    .body("Team not found.");
        }

        User user = userRepository
                .findById(member.getUserId())
                .orElse(null);

        if (user == null) {
            return ResponseEntity.badRequest()
                    .body("Student not found.");
        }

        Event event = eventRepository
                .findById(team.getEventId())
                .orElse(null);

        if (event == null) {
            return ResponseEntity.badRequest()
                    .body("Event not found.");
        }

        if (teamMemberRepository
                .findByTeamIdAndUserId(
                        member.getTeamId(),
                        member.getUserId())
                .isPresent()) {

            return ResponseEntity.badRequest()
                    .body("Student is already in this team.");
        }

        List<TeamMember> currentMembers =
                teamMemberRepository
                        .findByTeamId(member.getTeamId());

        int maxSize = event.getMaxTeamSize() == null
                ? 4
                : event.getMaxTeamSize();

        if (currentMembers.size() >= maxSize) {

            return ResponseEntity.badRequest()
                    .body("Maximum team size reached.");
        }

        if ("INTRA_DEPARTMENT".equalsIgnoreCase(
                event.getCompetitionType())) {

            if (event.getDepartmentName() != null &&
                    user.getDepartment() != null &&
                    !event.getDepartmentName()
                            .equalsIgnoreCase(
                                    user.getDepartment())) {

                return ResponseEntity.badRequest()
                        .body(
                            "For an intra-department event, "
                            + "student must belong to the same department."
                        );
            }
        }

        if (member.getMemberRole() == null ||
                member.getMemberRole().isBlank()) {

            member.setMemberRole("MEMBER");
        }

        return ResponseEntity.ok(
                teamMemberRepository.save(member)
        );
    }

    @GetMapping("/team/{teamId}")
    public ResponseEntity<List<TeamMember>>
            getTeamMembers(
                    @PathVariable Long teamId) {

        return ResponseEntity.ok(
                teamMemberRepository
                        .findByTeamId(teamId)
        );
    }

    @GetMapping("/student/{userId}")
    public ResponseEntity<List<TeamMember>>
            getStudentTeams(
                    @PathVariable Long userId) {

        return ResponseEntity.ok(
                teamMemberRepository
                        .findByUserId(userId)
        );
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> removeMember(
            @PathVariable Long id) {

        if (!teamMemberRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }

        teamMemberRepository.deleteById(id);

        return ResponseEntity.ok(
                "Team member removed."
        );
    }
}