package com.example.demo.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.example.demo.entity.Attendance;
import com.example.demo.repository.AttendanceRepository;

@RestController
@RequestMapping("/api/attendance")
@CrossOrigin(origins = "*")
public class AttendanceController {

    @Autowired
    private AttendanceRepository attendanceRepository;

    // =========================
    // ALL ATTENDANCE
    // =========================

    @GetMapping
    public ResponseEntity<List<Attendance>>
            getAllAttendance() {

        return ResponseEntity.ok(
                attendanceRepository.findAll()
        );
    }

    // =========================
    // USER ATTENDANCE
    // =========================

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<Attendance>>
            getUserAttendance(
                    @PathVariable Long userId) {

        return ResponseEntity.ok(
                attendanceRepository
                    .findByUserId(userId)
        );
    }

    // =========================
    // EVENT ATTENDANCE
    // =========================

    @GetMapping("/event/{eventId}")
    public ResponseEntity<List<Attendance>>
            getEventAttendance(
                    @PathVariable Long eventId) {

        return ResponseEntity.ok(
                attendanceRepository
                    .findByEventId(eventId)
        );
    }
}