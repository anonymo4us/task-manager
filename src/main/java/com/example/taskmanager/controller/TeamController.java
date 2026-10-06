package com.example.taskmanager.controller;

import com.example.taskmanager.model.Team;
import com.example.taskmanager.service.TeamService;
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
    public List<Team> findAll() {
        return teamService.findAll();
    }

    @GetMapping("/{id}")
    public Team findById(@PathVariable UUID id) {
        return teamService.findById(id);
    }

    @PostMapping
    public Team save(@RequestBody Team team) {
        return teamService.save(team);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable UUID id) {
        teamService.deleteById(id);
    }
}
