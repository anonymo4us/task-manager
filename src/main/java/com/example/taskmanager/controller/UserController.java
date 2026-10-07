package com.example.taskmanager.controller;

import com.example.taskmanager.dto.UserCreateRequest;
import com.example.taskmanager.dto.UserResponse;
import com.example.taskmanager.service.UserService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    public List<UserResponse> findAll() {
        return userService.findAll();
    }

    @GetMapping("/{id}")
    public UserResponse findById(@PathVariable UUID id) {
        return userService.findById(id);
    }

    @PostMapping
    public UserResponse save(@Valid @RequestBody UserCreateRequest request) {
        return userService.save(request);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable UUID id) {
        userService.deleteById(id);
    }
}
