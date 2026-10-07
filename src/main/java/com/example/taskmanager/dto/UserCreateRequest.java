package com.example.taskmanager.dto;

import jakarta.validation.constraints.NotBlank;

import java.util.UUID;

public record UserCreateRequest (
        @NotBlank(message = "Name must not be blank") String name,
        @NotBlank(message = "Department must not be blank") String department,
        UUID teamId
)
{}
