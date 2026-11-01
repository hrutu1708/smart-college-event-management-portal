package com.example.demo.repository;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.demo.entity.Team;

public interface TeamRepository
        extends JpaRepository<Team, Long> {

    List<Team> findByEventId(Long eventId);

    List<Team> findByCaptainUserId(Long captainUserId);
    @Transactional
    void deleteByEventId(Long eventId);
}