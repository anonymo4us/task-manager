package com.example.taskmanager.dto;

import java.util.UUID;

public record UserResponse (UUID id, String name, String department){
}
