package com.example.taskmanager.controller;

import com.example.taskmanager.dto.TeamCreateRequest;
import com.example.taskmanager.dto.TeamResponse;
import com.example.taskmanager.service.TeamService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/teams")
public class TeamController {

    private final TeamService teamService;

    public TeamController(TeamService teamService) {
        this.teamService = teamService;
    }

    @GetMapping
    public List<TeamResponse> findAll() {
        return teamService.findAll();
    }

    @GetMapping("/{id}")
    public TeamResponse findById(@PathVariable UUID id) {
        return teamService.findById(id);
    }

    @PostMapping
    public TeamResponse save(@Valid @RequestBody TeamCreateRequest request) {
        return teamService.save(request);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable UUID id) {
        teamService.deleteById(id);
    }
}
