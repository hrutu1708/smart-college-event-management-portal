package com.example.demo.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "teams")
public class Team {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    private Long eventId;

    private Long captainUserId;

    private String departmentName;

    private String opponentDepartmentName;

    private String status;

    public Team() {
    }

    @PrePersist
    public void beforeSave() {

        if (status == null || status.isBlank()) {
            status = "REGISTERED";
        }
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Long getEventId() {
        return eventId;
    }

    public void setEventId(Long eventId) {
        this.eventId = eventId;
    }

    public Long getCaptainUserId() {
        return captainUserId;
    }

    public void setCaptainUserId(Long captainUserId) {
        this.captainUserId = captainUserId;
    }

    public String getDepartmentName() {
        return departmentName;
    }

    public void setDepartmentName(String departmentName) {
        this.departmentName = departmentName;
    }

    public String getOpponentDepartmentName() {
        return opponentDepartmentName;
    }

    public void setOpponentDepartmentName(
            String opponentDepartmentName) {

        this.opponentDepartmentName =
                opponentDepartmentName;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}