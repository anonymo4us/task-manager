package com.example.taskmanager.dto;

import java.util.UUID;

public record TeamResponse(UUID id, String teamName, String specialization) {
}
