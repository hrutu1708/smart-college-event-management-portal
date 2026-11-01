package com.example.demo.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.example.demo.entity.Department;
import com.example.demo.repository.DepartmentRepository;

@RestController
@RequestMapping("/api/departments")
@CrossOrigin(origins = "*")
public class DepartmentController {

    @Autowired
    private DepartmentRepository departmentRepository;

    @GetMapping
    public ResponseEntity<List<Department>> getDepartments() {
        return ResponseEntity.ok(departmentRepository.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getDepartment(@PathVariable Long id) {

        return departmentRepository.findById(id)
                .<ResponseEntity<?>>map(ResponseEntity::ok)
                .orElseGet(() ->
                        ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<Department> createDepartment(
            @RequestBody Department department) {

        return ResponseEntity.ok(
                departmentRepository.save(department)
        );
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateDepartment(
            @PathVariable Long id,
            @RequestBody Department department) {

        return departmentRepository.findById(id)
                .map(existing -> {

                    existing.setName(department.getName());

                    return ResponseEntity.ok(
                            departmentRepository.save(existing)
                    );
                })
                .orElseGet(() ->
                        ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteDepartment(
            @PathVariable Long id) {

        if (!departmentRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }

        departmentRepository.deleteById(id);

        return ResponseEntity.ok(
                "Department deleted successfully."
        );
    }
}