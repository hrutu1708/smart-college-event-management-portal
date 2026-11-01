package com.example.demo.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.demo.entity.Result;

public interface ResultRepository
        extends JpaRepository<Result, Long> {

    List<Result> findByUserId(Long userId);

    List<Result> findByEventId(Long eventId);
}