package com.example.taskmanager.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;


public record ProjectCreateRequest(
        @NotBlank(message = "Project name must not be blank") String projectName,
        @NotNull(message = "Team ID must not be null") UUID teamId
)
{}
