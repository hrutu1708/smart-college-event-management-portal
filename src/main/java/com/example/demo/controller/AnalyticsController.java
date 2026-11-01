package com.example.demo.controller;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.repository.AttendanceRepository;
import com.example.demo.repository.EventRepository;
import com.example.demo.repository.FeedbackRepository;
import com.example.demo.repository.RegistrationRepository;
import com.example.demo.repository.UserRepository;

@RestController
@RequestMapping("/api/analytics")
@CrossOrigin(origins = "*")
public class AnalyticsController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private RegistrationRepository registrationRepository;

    @Autowired
    private AttendanceRepository attendanceRepository;

    @Autowired
    private FeedbackRepository feedbackRepository;

    @GetMapping
    public ResponseEntity<Map<String, Object>> getAnalytics() {

        Map<String, Object> analytics = new HashMap<>();

        analytics.put("totalStudents", userRepository.count());
        analytics.put("totalEvents", eventRepository.count());
        analytics.put("totalRegistrations", registrationRepository.count());
        analytics.put("totalAttendance", attendanceRepository.count());
        analytics.put("totalFeedback", feedbackRepository.count());

        return ResponseEntity.ok(analytics);
    }
}