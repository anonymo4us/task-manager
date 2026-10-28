
package com.example.taskmanager.controller;

import com.example.taskmanager.dto.request.TeamCreateRequest;
import com.example.taskmanager.dto.response.TeamResponse;
import com.example.taskmanager.exception.GlobalExceptionHandler;
import com.example.taskmanager.exception.ResourceNotFoundException;
import com.example.taskmanager.service.TeamService;
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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(TeamController.class)
@Import(GlobalExceptionHandler.class)
class TeamControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private TeamService teamService;

    @Test
    void findAll_returnsListOfTeams() throws Exception {
        // Arrange
        UUID teamId = UUID.randomUUID();
        TeamResponse teamResponse = new TeamResponse(teamId, "Backend", "Java");

        when(teamService.findAll()).thenReturn(List.of(teamResponse));

        // Act & Assert
        mockMvc.perform(get("/api/teams"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(teamId.toString()))
                .andExpect(jsonPath("$[0].teamName").value("Backend"))
                .andExpect(jsonPath("$[0].specialization").value("Java"));

        verify(teamService).findAll();
    }

    @Test
    void findAll_returnsEmptyList() throws Exception {
        // Arrange
        when(teamService.findAll()).thenReturn(List.of());

        // Act & Assert
        mockMvc.perform(get("/api/teams"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(0));

        verify(teamService).findAll();
    }

    @Test
    void findById_returnsTeamResponse() throws Exception {
        // Arrange
        UUID teamId = UUID.randomUUID();
        TeamResponse teamResponse = new TeamResponse(teamId, "Backend", "Java");

        when(teamService.findById(teamId)).thenReturn(teamResponse);

        // Act & Assert
        mockMvc.perform(get("/api/teams/{id}", teamId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(teamId.toString()))
                .andExpect(jsonPath("$.teamName").value("Backend"))
                .andExpect(jsonPath("$.specialization").value("Java"));

        verify(teamService).findById(teamId);
    }

    @Test
    void findById_whenTeamNotFound_returns404() throws Exception {
        // Arrange
        UUID teamId = UUID.randomUUID();

        when(teamService.findById(teamId))
                .thenThrow(new ResourceNotFoundException("Team not found: " + teamId));

        // Act & Assert
        mockMvc.perform(get("/api/teams/{id}", teamId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.message").value("Team not found: " + teamId));

        verify(teamService).findById(teamId);
    }

    @Test
    void save_createsTeam() throws Exception {
        // Arrange
        UUID teamId = UUID.randomUUID();
        TeamResponse teamResponse = new TeamResponse(teamId, "Backend", "Java");

        when(teamService.save(any(TeamCreateRequest.class))).thenReturn(teamResponse);

        String requestJson = """
                {
                  "teamName": "Backend",
                  "specialization": "Java"
                }
                """;

        // Act & Assert
        mockMvc.perform(post("/api/teams")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(teamId.toString()))
                .andExpect(jsonPath("$.teamName").value("Backend"))
                .andExpect(jsonPath("$.specialization").value("Java"));

        // Проверяем DTO, переданный контроллером в сервис
        ArgumentCaptor<TeamCreateRequest> captor =
                ArgumentCaptor.forClass(TeamCreateRequest.class);

        verify(teamService).save(captor.capture());

        TeamCreateRequest captured = captor.getValue();

        assertEquals("Backend", captured.teamName());
        assertEquals("Java", captured.specialization());
    }

    @Test
    void save_returnsBadRequestWhenTeamNameIsBlank() throws Exception {
        // Arrange
        String requestJson = """
                {
                  "teamName": " ",
                  "specialization": "Java"
                }
                """;

        // Act & Assert
        mockMvc.perform(post("/api/teams")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Validation Failed"))
                .andExpect(jsonPath("$.message").value("Team name must not be blank"));

        verifyNoInteractions(teamService);
    }

    @Test
    void save_returnsBadRequestWhenSpecializationIsBlank() throws Exception {
        // Arrange
        String requestJson = """
                {
                  "teamName": "Backend",
                  "specialization": " "
                }
                """;

        // Act & Assert
        mockMvc.perform(post("/api/teams")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Validation Failed"))
                .andExpect(jsonPath("$.message").value("Specialization must not be blank"));

        verifyNoInteractions(teamService);
    }

    @Test
    void save_returnsBadRequestWhenRequestBodyIsMalformed() throws Exception {
        // Arrange
        String malformedJson = """
                {"teamName":
                """;

        // Act & Assert
        mockMvc.perform(post("/api/teams")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(malformedJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message")
                        .value("Request body is missing or malformed"));

        verifyNoInteractions(teamService);
    }

    @Test
    void delete_deletesTeam() throws Exception {
        // Arrange
        UUID teamId = UUID.randomUUID();

        // Act & Assert
        mockMvc.perform(delete("/api/teams/{id}", teamId))
                .andExpect(status().isNoContent());

        verify(teamService).deleteById(teamId);
    }

    @Test
    void delete_returnsNotFoundWhenTeamDoesNotExist() throws Exception {
        // Arrange
        UUID teamId = UUID.randomUUID();

        doThrow(new ResourceNotFoundException("Team not found: " + teamId))
                .when(teamService).deleteById(teamId);

        // Act & Assert
        mockMvc.perform(delete("/api/teams/{id}", teamId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.message").value("Team not found: " + teamId));

        verify(teamService).deleteById(teamId);
    }
}
