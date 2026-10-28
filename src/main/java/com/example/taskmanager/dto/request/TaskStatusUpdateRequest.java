package com.example.taskmanager.dto.request;

import com.example.taskmanager.status.TaskStatus;
import jakarta.validation.constraints.NotNull;

public record TaskStatusUpdateRequest(
        @NotNull(message = "Status must not be null")
        TaskStatus status)
{}
