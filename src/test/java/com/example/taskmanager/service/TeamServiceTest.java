package com.example.taskmanager.service;

import com.example.taskmanager.dto.request.TeamCreateRequest;
import com.example.taskmanager.dto.response.TeamResponse;
import com.example.taskmanager.exception.ResourceConflictException;
import com.example.taskmanager.exception.ResourceNotFoundException;
import com.example.taskmanager.mapper.TeamMapper;
import com.example.taskmanager.model.Team;
import com.example.taskmanager.repository.TeamRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TeamServiceTest {

    @Mock
    private TeamRepository teamRepository;

    @Mock
    private TeamMapper teamMapper;

    @InjectMocks
    private TeamService teamService;

    @Test
    void findById_whenTeamExists_returnsTeamResponse() {
        // Arrange
        UUID teamId = UUID.randomUUID();

        Team team = new Team();
        team.setId(teamId);
        team.setTeamName("Backend");
        team.setSpecialization("Java");

        TeamResponse expectedResponse =
                new TeamResponse(teamId, "Backend", "Java");

        when(teamRepository.findById(teamId))
                .thenReturn(Optional.of(team));
        when(teamMapper.toResponse(team))
                .thenReturn(expectedResponse);

        // Act
        TeamResponse actualResponse = teamService.findById(teamId);

        // Assert
        assertEquals(expectedResponse, actualResponse);
        verify(teamRepository).findById(teamId);
        verify(teamMapper).toResponse(team);
    }

    @Test
    void findById_whenTeamNotFound_throwsResourceNotFoundException() {
        // Arrange
        UUID teamId = UUID.randomUUID();

        when(teamRepository.findById(teamId))
                .thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> teamService.findById(teamId))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Team not found: " + teamId);

        verify(teamRepository).findById(teamId);
        verifyNoInteractions(teamMapper);
    }

    @Test
    void findAll_whenTeamsExist_returnsListOfTeamResponses() {
        // Arrange
        UUID firstTeamId = UUID.randomUUID();
        UUID secondTeamId = UUID.randomUUID();

        Team firstTeam = new Team();
        firstTeam.setId(firstTeamId);
        firstTeam.setTeamName("Backend");
        firstTeam.setSpecialization("Java");

        Team secondTeam = new Team();
        secondTeam.setId(secondTeamId);
        secondTeam.setTeamName("QA");
        secondTeam.setSpecialization("Testing");

        TeamResponse firstResponse =
                new TeamResponse(firstTeamId, "Backend", "Java");
        TeamResponse secondResponse =
                new TeamResponse(secondTeamId, "QA", "Testing");

        List<TeamResponse> expectedResponses =
                List.of(firstResponse, secondResponse);

        when(teamRepository.findAll())
                .thenReturn(List.of(firstTeam, secondTeam));
        when(teamMapper.toResponse(firstTeam))
                .thenReturn(firstResponse);
        when(teamMapper.toResponse(secondTeam))
                .thenReturn(secondResponse);

        // Act
        List<TeamResponse> actualResponses = teamService.findAll();

        // Assert
        assertEquals(expectedResponses, actualResponses);
        verify(teamRepository).findAll();
        verify(teamMapper).toResponse(firstTeam);
        verify(teamMapper).toResponse(secondTeam);
    }

    @Test
    void findAll_whenNoTeamsExist_returnsEmptyList() {
        // Arrange
        when(teamRepository.findAll())
                .thenReturn(List.of());

        // Act
        List<TeamResponse> actualResponses = teamService.findAll();

        // Assert
        assertTrue(actualResponses.isEmpty());
        verify(teamRepository).findAll();
        verifyNoInteractions(teamMapper);
    }

    @Test
    void save_whenValidRequest_returnsTeamResponse() {
        // Arrange
        TeamCreateRequest request =
                new TeamCreateRequest("Backend", "Java");

        Team teamToSave = new Team();
        teamToSave.setTeamName("Backend");
        teamToSave.setSpecialization("Java");

        UUID savedTeamId = UUID.randomUUID();
        Team savedTeam = new Team();
        savedTeam.setId(savedTeamId);
        savedTeam.setTeamName("Backend");
        savedTeam.setSpecialization("Java");

        TeamResponse expectedResponse =
                new TeamResponse(savedTeamId, "Backend", "Java");

        when(teamMapper.toEntity(request))
                .thenReturn(teamToSave);
        when(teamRepository.save(teamToSave))
                .thenReturn(savedTeam);
        when(teamMapper.toResponse(savedTeam))
                .thenReturn(expectedResponse);

        // Act
        TeamResponse actualResponse = teamService.save(request);

        // Assert
        assertEquals(expectedResponse, actualResponse);
        verify(teamMapper).toEntity(request);
        verify(teamRepository).save(teamToSave);
        verify(teamMapper).toResponse(savedTeam);
    }

    @Test
    void deleteById_whenTeamExists_deletesTeam() {
        // Arrange
        UUID teamId = UUID.randomUUID();
        Team team = new Team();
        team.setId(teamId);

        when(teamRepository.findById(teamId))
                .thenReturn(Optional.of(team));

        // Act
        teamService.deleteById(teamId);

        // Assert
        verify(teamRepository).findById(teamId);
        verify(teamRepository).delete(team);
        verify(teamRepository).flush();
    }

    @Test
    void deleteById_whenTeamNotFound_throwsResourceNotFoundException() {
        // Arrange
        UUID teamId = UUID.randomUUID();
        when(teamRepository.findById(teamId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> teamService.deleteById(teamId))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Team not found: " + teamId);

       verify(teamRepository, never()).delete(any());
        verify(teamRepository, never()).flush();
    }

    @Test
    void deleteById_whenFlushThrowsDataIntegrityViolation_throwsResourceConflictException() {
        UUID teamId = UUID.randomUUID();
        Team team = new Team();
        team.setId(teamId);

        when(teamRepository.findById(teamId)).thenReturn(Optional.of(team));
        doThrow(new DataIntegrityViolationException("FK violation"))
                .when(teamRepository).flush();

        assertThatThrownBy(() -> teamService.deleteById(teamId))
                .isInstanceOf(ResourceConflictException.class)
                .hasMessageContaining("Team " + teamId + " cannot be deleted");

        verify(teamRepository).delete(team);
        verify(teamRepository).flush();
    }
}
