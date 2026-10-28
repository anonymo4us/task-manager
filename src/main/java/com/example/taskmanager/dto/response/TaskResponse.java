package com.example.taskmanager.dto.response;

import com.example.taskmanager.status.TaskStatus;

import java.util.Set;
import java.util.UUID;

public record TaskResponse(
        UUID id,
        String title,
        String description,
        TaskStatus status,
        UUID projectId,
        Set<UUID> assigneeIds
)
{}
