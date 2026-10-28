package com.example.taskmanager.controller;
import com.example.taskmanager.dto.request.UserCreateRequest;
import com.example.taskmanager.dto.response.UserResponse;
import com.example.taskmanager.exception.GlobalExceptionHandler;
import com.example.taskmanager.exception.ResourceNotFoundException;
import com.example.taskmanager.service.UserService;
import org.springframework.http.MediaType;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

@WebMvcTest(UserController.class)
@Import(GlobalExceptionHandler.class)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserService userService;

    @Test
    void findAll_returnsListOfUsers() throws Exception {
        // Arrange
        UUID userId = UUID.randomUUID();
        UserResponse userResponse = new UserResponse(userId, "Alice", "Engineering");

        when(userService.findAll()).thenReturn(List.of(userResponse));

        // Act & Assert
        mockMvc.perform(get("/api/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(userId.toString()))
                .andExpect(jsonPath("$[0].name").value("Alice"))
                .andExpect(jsonPath("$[0].department").value("Engineering"));

        verify(userService, times(1)).findAll();
    }

    @Test
    void findById_returnsUserResponse() throws Exception {
        // Arrange
        UUID userId = UUID.randomUUID();
        UserResponse userResponse = new UserResponse(userId, "Alice", "Engineering");

        when(userService.findById(userId)).thenReturn(userResponse);

        // Act & Assert
        mockMvc.perform(get("/api/users/{id}", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(userId.toString()))
                .andExpect(jsonPath("$.name").value("Alice"))
                .andExpect(jsonPath("$.department").value("Engineering"));

        verify(userService).findById(userId);
    }

    @Test
    void save_createsUser() throws Exception {
        // Arrange
        UUID userId = UUID.randomUUID();
        UserResponse userResponse = new UserResponse(userId, "Alice", "Engineering");

        when(userService.save(any(UserCreateRequest.class))).thenReturn(userResponse);

        String requestJson = """
            {
              "name": "Alice",
              "department": "Engineering",
              "teamId": null
            }
            """;

        // Act & Assert
        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(userId.toString()))
                .andExpect(jsonPath("$.name").value("Alice"))
                .andExpect(jsonPath("$.department").value("Engineering"));

        // Проверяем, что контроллер передал сервису корректный DTO
        ArgumentCaptor<UserCreateRequest> captor =
                ArgumentCaptor.forClass(UserCreateRequest.class);
        verify(userService).save(captor.capture());

        UserCreateRequest captured = captor.getValue();
        assertEquals("Alice", captured.name());
        assertEquals("Engineering", captured.department());
        assertNull(captured.teamId());
    }

    @Test
    void save_returnsBadRequestWhenNameIsBlank() throws Exception {
        // Arrange
        String requestJson = """
            {
              "name": " ",
              "department": "Engineering",
              "teamId": null
            }
            """;

        // Act & Assert
        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Validation Failed"))
                .andExpect(jsonPath("$.message").value("Name must not be blank"));

        // Валидация должна отклонить запрос до вызова сервиса
        verifyNoInteractions(userService);
    }

    @Test
    void save_returnsBadRequestWhenRequestBodyIsMalformed() throws Exception {
        // Arrange
        String malformedJson = """
            {"name":
            """;

        // Act & Assert
        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(malformedJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message").value("Request body is missing or malformed"));

        // Тело не дошло до сервиса — save не должен вызываться
        verifyNoInteractions(userService);
    }

    @Test
    void delete_deletesUser() throws Exception {
        // Arrange
        UUID userId = UUID.randomUUID();

        // Act & Assert
        mockMvc.perform(delete("/api/users/{id}", userId))
                .andExpect(status().isOk());

        verify(userService).deleteById(userId);
    }

    @Test
    void delete_returnsNotFoundWhenUserDoesNotExist() throws Exception {
        // Arrange
        UUID userId = UUID.randomUUID();

        doThrow(new ResourceNotFoundException("User not found: " + userId))
                .when(userService).deleteById(userId);

        // Act & Assert
        mockMvc.perform(delete("/api/users/{id}", userId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.message").value("User not found: " + userId));

        verify(userService).deleteById(userId);
    }

}