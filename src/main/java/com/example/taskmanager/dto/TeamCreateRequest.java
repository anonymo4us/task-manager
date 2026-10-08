package com.example.taskmanager.dto;

import jakarta.validation.constraints.NotBlank;

public record TeamCreateRequest(
        @NotBlank(message = "Team name must not be blank") String teamName,
        @NotBlank(message = "Specialization must not be blank") String specialization
        )
{}