package com.example.taskmanager.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.Set;
import java.util.UUID;

public record TaskCreateRequest(
        @NotBlank(message = "Task title must not be blank") String title,
        String description,
        UUID projectId,
        Set<@NotNull UUID> assigneeIds
) {
}
