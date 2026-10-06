package com.example.taskmanager.model;

import jakarta.persistence.*;
import org.hibernate.annotations.UuidGenerator;

import java.util.UUID;

@Entity
@Table(name = "users")
public class User {

    @Id
    @UuidGenerator
    private UUID id;

    private String name;

    private String department;

    @ManyToOne
    @JoinColumn(name = "team_id")
    private Team team;
}
