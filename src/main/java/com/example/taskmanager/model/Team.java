package com.example.taskmanager.model;

import jakarta.persistence.*;
import org.hibernate.annotations.UuidGenerator;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "teams")
public class Team {

    @Id
    @UuidGenerator
    private UUID id;

    @Column(name = "team_name", nullable = false)
    private String teamName;

    @Column(nullable = false)
    private String specialization;

    @OneToMany(mappedBy = "team")
    private Set<User> users = new HashSet<>();

    @OneToMany(mappedBy = "team")
    private Set<Project> projects = new HashSet<>();

    public Team() {
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getTeamName() {
        return teamName;
    }

    public void setTeamName(String teamName) {
        this.teamName = teamName;
    }

    public String getSpecialization() {
        return specialization;
    }

    public void setSpecialization(String specialization) {
        this.specialization = specialization;
    }
}
