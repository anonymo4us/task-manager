package com.example.taskmanager.dto;

import java.util.Set;
import java.util.UUID;

public record TaskResponse(
        UUID id,
        String title,
        String description,
        UUID projectId,
        Set<UUID> assigneeIds
)
{}
