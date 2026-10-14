package com.example.taskmanager.controller;

import com.example.taskmanager.dto.ProjectCreateRequest;
import com.example.taskmanager.dto.ProjectResponse;
import com.example.taskmanager.exception.GlobalExceptionHandler;
import com.example.taskmanager.exception.ResourceNotFoundException;
import com.example.taskmanager.service.ProjectService;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ProjectController.class)
@Import(GlobalExceptionHandler.class)
class ProjectControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ProjectService projectService;

    @Test
    void findAll_returnsListOfProjects() throws Exception {
        // Arrange
        UUID projectId = UUID.randomUUID();
        ProjectResponse projectResponse = new ProjectResponse(projectId, "Task Manager");

        when(projectService.findAll()).thenReturn(List.of(projectResponse));

        // Act & Assert
        mockMvc.perform(get("/api/projects"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(projectId.toString()))
                .andExpect(jsonPath("$[0].name").value("Task Manager"));

        verify(projectService).findAll();
    }

    @Test
    void findAll_returnsEmptyList() throws Exception {
        when(projectService.findAll()).thenReturn(List.of());

        mockMvc.perform(get("/api/projects"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(0));

        verify(projectService).findAll();
    }

    @Test
    void findById_returnsProjectResponse() throws Exception {
        UUID projectId = UUID.randomUUID();
        ProjectResponse response = new ProjectResponse(projectId, "Task Manager");

        when(projectService.findById(projectId)).thenReturn(response);

        mockMvc.perform(get("/api/projects/{id}", projectId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(projectId.toString()))
                .andExpect(jsonPath("$.name").value("Task Manager"));

        verify(projectService).findById(projectId);
    }

    @Test
    void findById_whenProjectNotFound_returns404() throws Exception{
        UUID projectId = UUID.randomUUID();

        when(projectService.findById(projectId))
                .thenThrow(new ResourceNotFoundException("Project not found: " + projectId));

        mockMvc.perform(get("/api/projects/{id}", projectId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.message").value("Project not found: " + projectId));

        verify(projectService).findById(projectId);
    }

    @Test
    void save_createsProject() throws Exception{
        UUID projectId = UUID.randomUUID();
        UUID teamId = UUID.randomUUID();
        ProjectResponse response = new ProjectResponse(projectId, "Task Manager");

        when(projectService.save(any(ProjectCreateRequest.class))).thenReturn(response);

        String requestJson = """
        {
          "projectName": "Task Manager",
          "teamId": "%s"
        }
        """.formatted(teamId);

        mockMvc.perform(post("/api/projects")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(projectId.toString()))
                .andExpect(jsonPath("$.name").value("Task Manager"));

        ArgumentCaptor<ProjectCreateRequest> captor =
                ArgumentCaptor.forClass(ProjectCreateRequest.class);
        verify(projectService).save(captor.capture());

        ProjectCreateRequest captured = captor.getValue();
        assertEquals("Task Manager", captured.projectName());
        assertEquals(teamId, captured.teamId());
    }

    @Test
    void save_returnsBadRequestWhenProjectNameIsBlank() throws Exception{
        String requestJson = """
        {
          "projectName": " ",
          "teamId": "%s"
        }
        """.formatted(UUID.randomUUID());

        mockMvc.perform(post("/api/projects")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Validation Failed"))
                .andExpect(jsonPath("$.message").value("Project name must not be blank"));

        verifyNoInteractions(projectService);
    }

    @Test
    void save_returnsBadRequestWhenTeamIdIsNull() throws Exception{
        String requestJson = """
        {
          "projectName": "Task Manager",
          "teamId": null
        }
        """;

        mockMvc.perform(post("/api/projects")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Validation Failed"))
                .andExpect(jsonPath("$.message").value("Team ID must not be null"));

        verifyNoInteractions(projectService);
    }

    @Test
    void save_returnsBadRequestWhenRequestBodyIsMalformed() throws Exception{
        String malformedJson = """
        {"projectName":
        """;

        mockMvc.perform(post("/api/projects")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(malformedJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message").value("Request body is missing or malformed"));

        verifyNoInteractions(projectService);
    }

    @Test
    void save_returnsNotFoundWhenTeamDoesNotExist() throws Exception{
        UUID teamId = UUID.randomUUID();

        String requestJson = """
        {
          "projectName": "Task Manager",
          "teamId": "%s"
        }
        """.formatted(teamId);

        when(projectService.save(any(ProjectCreateRequest.class)))
                .thenThrow(new ResourceNotFoundException("Team not found: " + teamId));
        mockMvc.perform(post("/api/projects")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.message").value("Team not found: " + teamId));
        verify(projectService).save(any(ProjectCreateRequest.class));
    }

    @Test
    void delete_deletesProject() throws Exception{
        UUID projectId = UUID.randomUUID();

        mockMvc.perform(delete("/api/projects/{id}", projectId))
                .andExpect(status().isOk());

        verify(projectService).deleteById(projectId);
    }

    @Test
    void delete_returnsNotFoundWhenProjectDoesNotExist() throws Exception{
        UUID projectId = UUID.randomUUID();

        doThrow(new ResourceNotFoundException("Project not found: " + projectId))
                .when(projectService).deleteById(projectId);

        mockMvc.perform(delete("/api/projects/{id}", projectId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.message").value("Project not found: " + projectId));

        verify(projectService).deleteById(projectId);
    }
}
