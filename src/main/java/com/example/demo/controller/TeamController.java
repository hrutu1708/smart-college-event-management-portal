package com.example.demo.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.example.demo.entity.Event;
import com.example.demo.entity.Team;
import com.example.demo.entity.TeamMember;
import com.example.demo.repository.EventRepository;
import com.example.demo.repository.TeamMemberRepository;
import com.example.demo.repository.TeamRepository;

@RestController
@RequestMapping("/api/teams")
@CrossOrigin(origins = "*")
public class TeamController {

    @Autowired
    private TeamRepository teamRepository;

    @Autowired
    private TeamMemberRepository teamMemberRepository;

    @Autowired
    private EventRepository eventRepository;

    @PostMapping
    public ResponseEntity<?> createTeam(
            @RequestBody Team team) {

        if (team.getEventId() == null) {
            return ResponseEntity.badRequest()
                    .body("Event ID is required.");
        }

        Event event = eventRepository
                .findById(team.getEventId())
                .orElse(null);

        if (event == null) {
            return ResponseEntity.badRequest()
                    .body("Event not found.");
        }

        if (!"GROUP".equalsIgnoreCase(
                event.getEventType())) {

            return ResponseEntity.badRequest()
                    .body("This event is not a group event.");
        }

        if (team.getCaptainUserId() == null) {
            return ResponseEntity.badRequest()
                    .body("Team captain is required.");
        }

        team.setDepartmentName(
                event.getDepartmentName()
        );

        team.setOpponentDepartmentName(
                event.getOpponentDepartmentName()
        );

        Team saved = teamRepository.save(team);

        return ResponseEntity.ok(saved);
    }

    @GetMapping
    public ResponseEntity<List<Team>> getAllTeams() {
        return ResponseEntity.ok(
                teamRepository.findAll()
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getTeam(
            @PathVariable Long id) {

        return teamRepository.findById(id)
                .<ResponseEntity<?>>map(team -> {

                    List<TeamMember> members =
                            teamMemberRepository
                                    .findByTeamId(id);

                    return ResponseEntity.ok(
                            new TeamDetails(team, members)
                    );
                })
                .orElseGet(() ->
                        ResponseEntity.notFound().build());
    }

    @GetMapping("/event/{eventId}")
    public ResponseEntity<List<Team>> getEventTeams(
            @PathVariable Long eventId) {

        return ResponseEntity.ok(
                teamRepository.findByEventId(eventId)
        );
    }

    @GetMapping("/captain/{userId}")
    public ResponseEntity<List<Team>> getCaptainTeams(
            @PathVariable Long userId) {

        return ResponseEntity.ok(
                teamRepository.findByCaptainUserId(userId)
        );
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateTeam(
            @PathVariable Long id,
            @RequestBody Team team) {

        return teamRepository.findById(id)
                .map(existing -> {

                    existing.setName(team.getName());

                    existing.setCaptainUserId(
                            team.getCaptainUserId()
                    );

                    existing.setStatus(
                            team.getStatus()
                    );

                    return ResponseEntity.ok(
                            teamRepository.save(existing)
                    );
                })
                .orElseGet(() ->
                        ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteTeam(
            @PathVariable Long id) {

        if (!teamRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }

        teamMemberRepository
                .findByTeamId(id)
                .forEach(teamMemberRepository::delete);

        teamRepository.deleteById(id);

        return ResponseEntity.ok(
                "Team deleted successfully."
        );
    }

    public static class TeamDetails {

        private Team team;
        private List<TeamMember> members;

        public TeamDetails(
                Team team,
                List<TeamMember> members) {

            this.team = team;
            this.members = members;
        }

        public Team getTeam() {
            return team;
        }

        public List<TeamMember> getMembers() {
            return members;
        }
    }
}