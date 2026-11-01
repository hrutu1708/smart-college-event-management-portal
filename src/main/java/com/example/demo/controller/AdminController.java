package com.example.demo.controller;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import com.example.demo.entity.User;
import com.example.demo.repository.UserRepository;

@RestController
@RequestMapping("/api/admin")
@CrossOrigin(origins = "*")
public class AdminController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    // =========================
    // CREATE ADMIN
    // =========================

    @PostMapping("/create")
    public ResponseEntity<?> createAdmin(
            @RequestBody User user) {

        if (user.getName() == null ||
                user.getName().isBlank()) {

            return ResponseEntity.badRequest()
                    .body("Admin name is required.");
        }

        if (user.getEmail() == null ||
                user.getEmail().isBlank()) {

            return ResponseEntity.badRequest()
                    .body("Admin email is required.");
        }

        if (user.getPassword() == null ||
                user.getPassword().isBlank()) {

            return ResponseEntity.badRequest()
                    .body("Admin password is required.");
        }

        if (userRepository.existsByEmail(
                user.getEmail())) {

            return ResponseEntity.badRequest()
                    .body(
                        "An account with this email already exists."
                    );
        }

        User admin = new User();

        admin.setName(user.getName());
        admin.setEmail(user.getEmail());

        admin.setPassword(
                passwordEncoder.encode(
                        user.getPassword()
                )
        );

        admin.setRole("ADMIN");

        User saved =
                userRepository.save(admin);

        Map<String, Object> response =
                new HashMap<>();

        response.put(
                "message",
                "Administrator created successfully."
        );

        response.put(
                "id",
                saved.getId()
        );

        response.put(
                "name",
                saved.getName()
        );

        response.put(
                "email",
                saved.getEmail()
        );

        response.put(
                "role",
                saved.getRole()
        );

        return ResponseEntity.ok(response);
    }

    // =========================
    // ADMIN STATS
    // =========================

    @GetMapping("/stats")
    public ResponseEntity<?> getStats() {

        Map<String, Object> response =
                new HashMap<>();

        response.put(
                "totalUsers",
                userRepository.count()
        );

        return ResponseEntity.ok(response);
    }
}