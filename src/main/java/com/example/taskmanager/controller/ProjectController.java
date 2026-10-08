package com.example.taskmanager.controller;

import com.example.taskmanager.dto.ProjectCreateRequest;
import com.example.taskmanager.dto.ProjectResponse;
import com.example.taskmanager.service.ProjectService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/projects")
public class ProjectController {

    private final ProjectService projectService;

    public ProjectController(ProjectService projectService) {
        this.projectService = projectService;
    }

    @GetMapping
    public List<ProjectResponse> findAll() {
        return projectService.findAll();
    }

    @GetMapping("/{id}")
    public ProjectResponse findById(@PathVariable UUID id) {
        return projectService.findById(id);
    }

    @PostMapping
    public ProjectResponse save(@Valid @RequestBody ProjectCreateRequest request) {
        return projectService.save(request);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable UUID id) {
        projectService.deleteById(id);
    }
}
