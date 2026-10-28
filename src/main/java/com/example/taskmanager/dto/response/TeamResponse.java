package com.example.taskmanager.dto.response;

import java.util.UUID;

public record TeamResponse(
        UUID id,
        String teamName,
        String specialization) {
}
