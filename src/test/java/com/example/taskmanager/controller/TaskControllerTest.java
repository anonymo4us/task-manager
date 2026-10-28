package com.example.taskmanager.controller;

import com.example.taskmanager.dto.request.TaskCreateRequest;
import com.example.taskmanager.dto.response.TaskResponse;
import com.example.taskmanager.exception.GlobalExceptionHandler;
import com.example.taskmanager.exception.InvalidTaskStatusTransitionException;
import com.example.taskmanager.exception.ResourceNotFoundException;
import com.example.taskmanager.service.TaskService;
import com.example.taskmanager.status.TaskStatus;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(TaskController.class)
@Import(GlobalExceptionHandler.class)
class TaskControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private TaskService taskService;

    @Test
    void findAll_returnsListOfTasks() throws Exception {
        // Arrange
        UUID taskId = UUID.randomUUID();
        UUID projectId = UUID.randomUUID();
        UUID assigneeId = UUID.randomUUID();

        TaskResponse taskResponse = new TaskResponse(
                taskId,
                "Fix bug",
                "Critical bug",
                TaskStatus.TODO,
                projectId,
                Set.of(assigneeId)
        );

        when(taskService.findAll()).thenReturn(List.of(taskResponse));

        // Act & Assert
        mockMvc.perform(get("/api/tasks"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(taskId.toString()))
                .andExpect(jsonPath("$[0].title").value("Fix bug"))
                .andExpect(jsonPath("$[0].description").value("Critical bug"))
                .andExpect(jsonPath("$[0].projectId").value(projectId.toString()))
                .andExpect(jsonPath("$[0].assigneeIds.length()").value(1))
                .andExpect(jsonPath("$[0].assigneeIds[0]").value(assigneeId.toString()));

        verify(taskService).findAll();
    }

    @Test
    void findAll_returnsEmptyList() throws Exception {
        // Arrange
        when(taskService.findAll()).thenReturn(List.of());

        // Act & Assert
        mockMvc.perform(get("/api/tasks"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(0));

        verify(taskService).findAll();
    }

    @Test
    void findById_returnsTaskResponse() throws Exception {
        // Arrange
        UUID taskId = UUID.randomUUID();
        UUID projectId = UUID.randomUUID();
        UUID assigneeId = UUID.randomUUID();

        TaskResponse taskResponse = new TaskResponse(
                taskId,
                "Fix bug",
                "Critical bug",
                TaskStatus.TODO,
                projectId,
                Set.of(assigneeId)
        );

        when(taskService.findById(taskId)).thenReturn(taskResponse);

        // Act & Assert
        mockMvc.perform(get("/api/tasks/{id}", taskId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(taskId.toString()))
                .andExpect(jsonPath("$.title").value("Fix bug"))
                .andExpect(jsonPath("$.description").value("Critical bug"))
                .andExpect(jsonPath("$.projectId").value(projectId.toString()))
                .andExpect(jsonPath("$.assigneeIds.length()").value(1))
                .andExpect(jsonPath("$.assigneeIds[0]").value(assigneeId.toString()));

        verify(taskService).findById(taskId);
    }

    @Test
    void findById_whenTaskNotFound_returns404() throws Exception {
        // Arrange
        UUID taskId = UUID.randomUUID();

        when(taskService.findById(taskId))
                .thenThrow(new ResourceNotFoundException("Task not found: " + taskId));

        // Act & Assert
        mockMvc.perform(get("/api/tasks/{id}", taskId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.message").value("Task not found: " + taskId));

        verify(taskService).findById(taskId);
    }

    @Test
    void save_createsTaskWithProjectAndAssignees() throws Exception {
        // Arrange
        UUID taskId = UUID.randomUUID();
        UUID projectId = UUID.randomUUID();
        UUID firstUserId = UUID.randomUUID();
        UUID secondUserId = UUID.randomUUID();

        TaskResponse taskResponse = new TaskResponse(
                taskId,
                "Fix bug",
                "Critical bug",
                TaskStatus.TODO,
                projectId,
                Set.of(firstUserId, secondUserId)
        );

        when(taskService.save(any(TaskCreateRequest.class))).thenReturn(taskResponse);

        String requestJson = """
                {
                  "title": "Fix bug",
                  "description": "Critical bug",
                  "projectId": "%s",
                  "assigneeIds": ["%s", "%s"]
                }
                """.formatted(projectId, firstUserId, secondUserId);

        // Act & Assert
        mockMvc.perform(post("/api/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(taskId.toString()))
                .andExpect(jsonPath("$.title").value("Fix bug"))
                .andExpect(jsonPath("$.description").value("Critical bug"))
                .andExpect(jsonPath("$.projectId").value(projectId.toString()))
                .andExpect(jsonPath("$.assigneeIds.length()").value(2));

        ArgumentCaptor<TaskCreateRequest> captor =
                ArgumentCaptor.forClass(TaskCreateRequest.class);
        verify(taskService).save(captor.capture());

        TaskCreateRequest captured = captor.getValue();
        assertEquals("Fix bug", captured.title());
        assertEquals("Critical bug", captured.description());
        assertEquals(projectId, captured.projectId());
        assertEquals(Set.of(firstUserId, secondUserId), captured.assigneeIds());
    }

    @Test
    void save_createsTaskWithoutProjectAndAssignees() throws Exception {
        // Arrange
        UUID taskId = UUID.randomUUID();

        TaskResponse taskResponse = new TaskResponse(
                taskId,
                "Fix bug",
                null,
                TaskStatus.TODO,
                null,
                Set.of()
        );

        when(taskService.save(any(TaskCreateRequest.class))).thenReturn(taskResponse);

        String requestJson = """
                {
                  "title": "Fix bug"
                }
                """;

        // Act & Assert
        mockMvc.perform(post("/api/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(taskId.toString()))
                .andExpect(jsonPath("$.title").value("Fix bug"))
                .andExpect(jsonPath("$.assigneeIds.length()").value(0));

        ArgumentCaptor<TaskCreateRequest> captor =
                ArgumentCaptor.forClass(TaskCreateRequest.class);
        verify(taskService).save(captor.capture());

        TaskCreateRequest captured = captor.getValue();
        assertEquals("Fix bug", captured.title());
        assertNull(captured.description());
        assertNull(captured.projectId());
        assertNull(captured.assigneeIds());
    }

    @Test
    void save_returnsBadRequestWhenTitleIsBlank() throws Exception {
        // Arrange
        String requestJson = """
                {
                  "title": " ",
                  "description": "Critical bug"
                }
                """;

        // Act & Assert
        mockMvc.perform(post("/api/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Validation Failed"))
                .andExpect(jsonPath("$.message").value("Task title must not be blank"));

        verifyNoInteractions(taskService);
    }

    @Test
    void save_returnsBadRequestWhenRequestBodyIsMalformed() throws Exception {
        // Arrange
        String malformedJson = """
                {"title":
                """;

        // Act & Assert
        mockMvc.perform(post("/api/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(malformedJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message").value("Request body is missing or malformed"));

        verifyNoInteractions(taskService);
    }

    @Test
    void save_returnsNotFoundWhenProjectDoesNotExist() throws Exception {
        // Arrange
        UUID projectId = UUID.randomUUID();

        String requestJson = """
                {
                  "title": "Fix bug",
                  "projectId": "%s"
                }
                """.formatted(projectId);

        when(taskService.save(any(TaskCreateRequest.class)))
                .thenThrow(new ResourceNotFoundException("Project not found: " + projectId));

        // Act & Assert
        mockMvc.perform(post("/api/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.message").value("Project not found: " + projectId));

        verify(taskService).save(any(TaskCreateRequest.class));
    }

    @Test
    void save_returnsNotFoundWhenAssigneeDoesNotExist() throws Exception {
        // Arrange
        UUID projectId = UUID.randomUUID();
        UUID firstUserId = UUID.randomUUID();
        UUID secondUserId = UUID.randomUUID();

        String requestJson = """
                {
                  "title": "Fix bug",
                  "projectId": "%s",
                  "assigneeIds": ["%s", "%s"]
                }
                """.formatted(projectId, firstUserId, secondUserId);

        when(taskService.save(any(TaskCreateRequest.class)))
                .thenThrow(new ResourceNotFoundException("One or more users not found"));

        // Act & Assert
        mockMvc.perform(post("/api/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.message").value("One or more users not found"));

        verify(taskService).save(any(TaskCreateRequest.class));
    }

    @Test
    void delete_deletesTask() throws Exception {
        // Arrange
        UUID taskId = UUID.randomUUID();

        // Act & Assert
        mockMvc.perform(delete("/api/tasks/{id}", taskId))
                .andExpect(status().isNoContent());

        verify(taskService).deleteById(taskId);
    }

    @Test
    void delete_returnsNotFoundWhenTaskDoesNotExist() throws Exception {
        // Arrange
        UUID taskId = UUID.randomUUID();

        doThrow(new ResourceNotFoundException("Task not found: " + taskId))
                .when(taskService).deleteById(taskId);

        // Act & Assert
        mockMvc.perform(delete("/api/tasks/{id}", taskId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.message").value("Task not found: " + taskId));

        verify(taskService).deleteById(taskId);
    }

    @Test
    void updateStatus_returnsUpdatedTask() throws Exception {
        // Arrange
        UUID taskId = UUID.randomUUID();
        UUID projectId = UUID.randomUUID();
        UUID assigneeId = UUID.randomUUID();

        TaskResponse taskResponse = new TaskResponse(
                taskId,
                "Fix bug",
                "Critical bug",
                TaskStatus.IN_PROGRESS,
                projectId,
                Set.of(assigneeId)
        );

        when(taskService.updateStatus(taskId, TaskStatus.IN_PROGRESS))
                .thenReturn(taskResponse);

        String requestJson = """
            {
              "status": "IN_PROGRESS"
            }
            """;

        // Act & Assert
        mockMvc.perform(patch("/api/tasks/{id}/status", taskId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(taskId.toString()))
                .andExpect(jsonPath("$.title").value("Fix bug"))
                .andExpect(jsonPath("$.status").value("IN_PROGRESS"));

        verify(taskService).updateStatus(taskId, TaskStatus.IN_PROGRESS);
    }

    @Test
    void updateStatus_whenTaskNotFound_returns404() throws Exception {
        // Arrange
        UUID taskId = UUID.randomUUID();
        when(taskService.updateStatus(taskId, TaskStatus.IN_PROGRESS))
                .thenThrow(new ResourceNotFoundException("Task not found: " + taskId));

        String requestJson = """
            {
              "status": "IN_PROGRESS"
            }
            """;

        // Act & Assert
        mockMvc.perform(patch("/api/tasks/{id}/status", taskId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.message").value("Task not found: " + taskId));

        verify(taskService).updateStatus(taskId, TaskStatus.IN_PROGRESS);
    }

    @Test
    void updateStatus_whenTransitionForbidden_returns409() throws Exception {
        // Arrange
        UUID taskId = UUID.randomUUID();
        when(taskService.updateStatus(taskId, TaskStatus.DONE))
                .thenThrow(new InvalidTaskStatusTransitionException(
                        "Cannot change status from TODO to DONE"));

        String requestJson = """
            {
              "status": "DONE"
            }
            """;

        // Act & Assert
        mockMvc.perform(patch("/api/tasks/{id}/status", taskId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value("Conflict"))
                .andExpect(jsonPath("$.message")
                        .value("Cannot change status from TODO to DONE"));

        verify(taskService).updateStatus(taskId, TaskStatus.DONE);
    }

    @Test
    void updateStatus_whenStatusIsNull_returns400() throws Exception {
        // Arrange
        UUID taskId = UUID.randomUUID();
        String requestJson = """
            {
              "status": null
            }
            """;

        // Act & Assert
        mockMvc.perform(patch("/api/tasks/{id}/status", taskId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Validation Failed"))
                .andExpect(jsonPath("$.message").value("Status must not be null"));

        verifyNoInteractions(taskService);
    }
}