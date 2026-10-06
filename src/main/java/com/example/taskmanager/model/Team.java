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

    private String teamName;

    private String specialization;

    @OneToMany(mappedBy = "team")
    private Set<User> users = new HashSet<>();

    @OneToMany(mappedBy = "team")
    private Set<Project> projects = new HashSet<>();
}
