package com.example.demo.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.example.demo.entity.Result;
import com.example.demo.repository.ResultRepository;

@RestController
@RequestMapping("/api/results")
@CrossOrigin(origins = "*")
public class ResultController {

    @Autowired
    private ResultRepository repository;

    @PostMapping
    public ResponseEntity<Result> create(
            @RequestBody Result result) {

        return ResponseEntity.ok(
                repository.save(result)
        );
    }

    @GetMapping
    public ResponseEntity<List<Result>> getAll() {

        return ResponseEntity.ok(
                repository.findAll()
        );
    }
}