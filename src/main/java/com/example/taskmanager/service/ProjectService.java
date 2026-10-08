package com.example.taskmanager.service;

import com.example.taskmanager.dto.ProjectCreateRequest;
import com.example.taskmanager.dto.ProjectResponse;
import com.example.taskmanager.exception.ResourceNotFoundException;
import com.example.taskmanager.mapper.ProjectMapper;
import com.example.taskmanager.model.Project;
import com.example.taskmanager.model.Team;
import com.example.taskmanager.repository.ProjectRepository;
import com.example.taskmanager.repository.TeamRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class ProjectService {

    private final ProjectRepository projectRepository;
    private final ProjectMapper projectMapper;
    private final TeamRepository teamRepository;

    public ProjectService(ProjectRepository projectRepository, ProjectMapper projectMapper, TeamRepository teamRepository) {
        this.projectRepository = projectRepository;
        this.projectMapper = projectMapper;
        this.teamRepository = teamRepository;
    }

    public List<ProjectResponse> findAll() {
        return projectRepository.findAll()
                .stream()
                .map(projectMapper::toResponse)
                .toList();
    }

    public ProjectResponse findById(UUID id) {
        return projectRepository.findById(id)
                .map(projectMapper::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found: " + id));
    }

    public ProjectResponse save(ProjectCreateRequest request) {
        Team team = resolveTeam(request.teamId());
        Project project = projectMapper.toEntity(request, team);
        Project savedProject = projectRepository.save(project);
        return projectMapper.toResponse(savedProject);
    }

    private Team resolveTeam(UUID teamId) {
        return teamRepository.findById(teamId)
                .orElseThrow(() -> new ResourceNotFoundException("Team not found"));
    }

    public void deleteById(UUID id) {
        if (!projectRepository.existsById(id)) {
            throw new ResourceNotFoundException("Project not found: " + id);
        }
        projectRepository.deleteById(id);
    }
}
