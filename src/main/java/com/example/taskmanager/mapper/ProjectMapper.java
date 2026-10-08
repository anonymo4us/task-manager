package com.example.taskmanager.mapper;

import com.example.taskmanager.dto.ProjectCreateRequest;
import com.example.taskmanager.dto.ProjectResponse;
import com.example.taskmanager.model.Project;
import com.example.taskmanager.model.Team;
import org.springframework.stereotype.Component;

@Component
public class ProjectMapper {
    public Project toEntity(ProjectCreateRequest request, Team team) {
        Project project = new Project();
        project.setName(request.projectName());
        project.setTeam(team);
        return project;
    }
    public ProjectResponse toResponse(Project project) {
        return new ProjectResponse(
                project.getId(),
                project.getName()
        );
    }
}
