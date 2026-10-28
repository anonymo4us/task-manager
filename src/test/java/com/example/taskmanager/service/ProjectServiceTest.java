package com.example.taskmanager.service;

import com.example.taskmanager.dto.request.ProjectCreateRequest;
import com.example.taskmanager.dto.response.ProjectResponse;
import com.example.taskmanager.exception.ResourceNotFoundException;
import com.example.taskmanager.mapper.ProjectMapper;
import com.example.taskmanager.model.Project;
import com.example.taskmanager.model.Team;
import com.example.taskmanager.repository.ProjectRepository;
import com.example.taskmanager.repository.TeamRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProjectServiceTest {

    @Mock
    private ProjectRepository projectRepository;

    @Mock
    private ProjectMapper projectMapper;

    @Mock
    private TeamRepository teamRepository;

    @InjectMocks
    private ProjectService projectService;

    @Test
    void findById_whenProjectExists_returnsProjectResponse() {
        // Arrange
        UUID projectId = UUID.randomUUID();

        Project project = new Project();
        project.setId(projectId);
        project.setName("Task Manager");

        ProjectResponse expectedResponse =
                new ProjectResponse(projectId, "Task Manager");

        when(projectRepository.findById(projectId))
                .thenReturn(Optional.of(project));
        when(projectMapper.toResponse(project))
                .thenReturn(expectedResponse);

        // Act
        ProjectResponse actualResponse = projectService.findById(projectId);

        // Assert
        assertEquals(expectedResponse, actualResponse);
        verify(projectRepository).findById(projectId);
        verify(projectMapper).toResponse(project);
    }

    @Test
    void findById_whenProjectNotFound_throwsResourceNotFoundException() {
        // Arrange
        UUID projectId = UUID.randomUUID();

        when(projectRepository.findById(projectId))
                .thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> projectService.findById(projectId))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Project not found: " + projectId);

        verify(projectRepository).findById(projectId);
        verifyNoInteractions(projectMapper);
    }

    @Test
    void findAll_whenProjectsExist_returnsListOfProjectResponses() {
        // Arrange
        UUID firstProjectId = UUID.randomUUID();
        UUID secondProjectId = UUID.randomUUID();

        Project firstProject = new Project();
        firstProject.setId(firstProjectId);
        firstProject.setName("Task Manager");

        Project secondProject = new Project();
        secondProject.setId(secondProjectId);
        secondProject.setName("CRM");

        ProjectResponse firstResponse =
                new ProjectResponse(firstProjectId, "Task Manager");
        ProjectResponse secondResponse =
                new ProjectResponse(secondProjectId, "CRM");

        List<ProjectResponse> expectedResponses =
                List.of(firstResponse, secondResponse);

        when(projectRepository.findAll())
                .thenReturn(List.of(firstProject, secondProject));
        when(projectMapper.toResponse(firstProject))
                .thenReturn(firstResponse);
        when(projectMapper.toResponse(secondProject))
                .thenReturn(secondResponse);

        // Act
        List<ProjectResponse> actualResponses = projectService.findAll();

        // Assert
        assertEquals(expectedResponses, actualResponses);
        verify(projectRepository).findAll();
        verify(projectMapper).toResponse(firstProject);
        verify(projectMapper).toResponse(secondProject);
    }

    @Test
    void findAll_whenNoProjectsExist_returnsEmptyList() {
        // Arrange
        when(projectRepository.findAll())
                .thenReturn(List.of());

        // Act
        List<ProjectResponse> actualResponses = projectService.findAll();

        // Assert
        assertTrue(actualResponses.isEmpty());
        verify(projectRepository).findAll();
        verifyNoInteractions(projectMapper);
    }

    @Test
    void save_whenValidRequest_returnsProjectResponse() {
        // Arrange
        UUID teamId = UUID.randomUUID();
        Team team = new Team();
        team.setId(teamId);
        team.setTeamName("Backend");
        team.setSpecialization("Java");

        ProjectCreateRequest request =
                new ProjectCreateRequest("Task Manager", teamId);

        Project projectToSave = new Project();
        projectToSave.setName("Task Manager");
        projectToSave.setTeam(team);

        UUID savedProjectId = UUID.randomUUID();
        Project savedProject = new Project();
        savedProject.setId(savedProjectId);
        savedProject.setName("Task Manager");
        savedProject.setTeam(team);

        ProjectResponse expectedResponse =
                new ProjectResponse(savedProjectId, "Task Manager");

        when(teamRepository.findById(teamId))
                .thenReturn(Optional.of(team));
        when(projectMapper.toEntity(request, team))
                .thenReturn(projectToSave);
        when(projectRepository.save(projectToSave))
                .thenReturn(savedProject);
        when(projectMapper.toResponse(savedProject))
                .thenReturn(expectedResponse);

        // Act
        ProjectResponse actualResponse = projectService.save(request);

        // Assert
        assertEquals(expectedResponse, actualResponse);

        verify(teamRepository).findById(teamId);
        verify(projectMapper).toEntity(request, team);
        verify(projectRepository).save(projectToSave);
        verify(projectMapper).toResponse(savedProject);
    }

    @Test
    void save_whenTeamNotFound_throwsResourceNotFoundException() {
        // Arrange
        UUID missingTeamId = UUID.randomUUID();

        ProjectCreateRequest request =
                new ProjectCreateRequest("Task Manager", missingTeamId);

        when(teamRepository.findById(missingTeamId))
                .thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> projectService.save(request))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Team not found: " + missingTeamId);

        verify(teamRepository).findById(missingTeamId);
        verifyNoInteractions(projectMapper);
        verifyNoInteractions(projectRepository);
    }

    @Test
    void deleteById_whenProjectExists_deletesProject() {
        // Arrange
        UUID projectId = UUID.randomUUID();

        when(projectRepository.existsById(projectId))
                .thenReturn(true);

        // Act
        projectService.deleteById(projectId);

        // Assert
        verify(projectRepository).existsById(projectId);
        verify(projectRepository).deleteById(projectId);
    }

    @Test
    void deleteById_whenProjectNotFound_throwsResourceNotFoundException() {
        // Arrange
        UUID projectId = UUID.randomUUID();

        when(projectRepository.existsById(projectId))
                .thenReturn(false);

        // Act & Assert
        assertThatThrownBy(() -> projectService.deleteById(projectId))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Project not found: " + projectId);

        verify(projectRepository).existsById(projectId);
        verify(projectRepository, never()).deleteById(projectId);
    }

}
