package com.example.taskmanager.service;

import com.example.taskmanager.dto.UserCreateRequest;
import com.example.taskmanager.dto.UserResponse;
import com.example.taskmanager.exception.ResourceNotFoundException;
import com.example.taskmanager.mapper.UserMapper;
import com.example.taskmanager.model.Team;
import com.example.taskmanager.model.User;
import com.example.taskmanager.repository.TeamRepository;
import com.example.taskmanager.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserMapper userMapper;

    @Mock
    private UserRepository userRepository;

    @Mock
    private TeamRepository teamRepository;

    @InjectMocks
    private UserService userService;

    @Test
    void findById_whenUserExists_returnsUserResponse() {
        // Arrange
        UUID userId = UUID.randomUUID();

        User user = new User();
        user.setId(userId);
        user.setName("Maxim");
        user.setDepartment("Engineering");

        UserResponse expectedResponse =
                new UserResponse(userId, "Maxim", "Engineering");

        when(userRepository.findById(userId))
                .thenReturn(Optional.of(user));
        when(userMapper.toResponse(user))
                .thenReturn(expectedResponse);

        // Act
        UserResponse actualResponse = userService.findById(userId);

        // Assert
        assertEquals(expectedResponse, actualResponse);
        verify(userRepository).findById(userId);
        verify(userMapper).toResponse(user);
    }

    @Test
    void findById_whenUserNotFound_throwsResourceNotFoundException() {
        // Arrange
        UUID userId = UUID.randomUUID();

        when(userRepository.findById(userId))
                .thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> userService.findById(userId))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("User not found: " + userId);

        verify(userRepository).findById(userId);
        verifyNoInteractions(userMapper);
    }

    @Test
    void findAll_whenUsersExist_returnsListOfUserResponses() {
        // Arrange
        UUID firstUserId = UUID.randomUUID();
        UUID secondUserId = UUID.randomUUID();

        User firstUser = new User();
        firstUser.setId(firstUserId);
        firstUser.setName("Maxim");
        firstUser.setDepartment("Engineering");

        User secondUser = new User();
        secondUser.setId(secondUserId);
        secondUser.setName("Ivan");
        secondUser.setDepartment("QA");

        UserResponse firstResponse =
                new UserResponse(firstUserId, "Maxim", "Engineering");
        UserResponse secondResponse =
                new UserResponse(secondUserId, "Ivan", "QA");

        List<UserResponse> expectedResponses =
                List.of(firstResponse, secondResponse);

        when(userRepository.findAll())
                .thenReturn(List.of(firstUser, secondUser));
        when(userMapper.toResponse(firstUser))
                .thenReturn(firstResponse);
        when(userMapper.toResponse(secondUser))
                .thenReturn(secondResponse);

        // Act
        List<UserResponse> actualResponses = userService.findAll();

        // Assert
        assertEquals(expectedResponses, actualResponses);
        verify(userRepository).findAll();
        verify(userMapper).toResponse(firstUser);
        verify(userMapper).toResponse(secondUser);
    }

    @Test
    void save_whenTeamIdIsNull_createsUserWithoutTeam() {
        // Arrange
        UserCreateRequest request =
                new UserCreateRequest("Alice", "Engineering", null);

        User userToSave = new User();
        userToSave.setName("Alice");
        userToSave.setDepartment("Engineering");

        User savedUser = new User();
        savedUser.setId(UUID.randomUUID());
        savedUser.setName("Alice");
        savedUser.setDepartment("Engineering");

        UserResponse expectedResponse = new UserResponse(
                savedUser.getId(),
                "Alice",
                "Engineering"
        );

        when(userMapper.toEntity(request, null))
                .thenReturn(userToSave);
        when(userRepository.save(userToSave))
                .thenReturn(savedUser);
        when(userMapper.toResponse(savedUser))
                .thenReturn(expectedResponse);

        // Act
        UserResponse actualResponse = userService.save(request);

        // Assert
        assertEquals(expectedResponse, actualResponse);
        verify(userRepository).save(userToSave);
        verify(userMapper).toEntity(request, null);
        verify(userMapper).toResponse(savedUser);
        verifyNoInteractions(teamRepository);
    }

    @Test
    void save_whenTeamExists_createsUserWithTeam() {
        // Arrange
        UUID teamId = UUID.randomUUID();

        Team team = new Team();
        team.setId(teamId);
        team.setTeamName("Backend");
        team.setSpecialization("Java");

        UserCreateRequest request =
                new UserCreateRequest("Alice", "Engineering", teamId);

        User userToSave = new User();
        userToSave.setName("Alice");
        userToSave.setDepartment("Engineering");
        userToSave.setTeam(team);

        User savedUser = new User();
        savedUser.setId(UUID.randomUUID());
        savedUser.setName("Alice");
        savedUser.setDepartment("Engineering");
        savedUser.setTeam(team);

        UserResponse expectedResponse = new UserResponse(
                savedUser.getId(),
                "Alice",
                "Engineering"
        );

        when(teamRepository.findById(teamId))
                .thenReturn(Optional.of(team));
        when(userMapper.toEntity(request, team))
                .thenReturn(userToSave);
        when(userRepository.save(userToSave))
                .thenReturn(savedUser);
        when(userMapper.toResponse(savedUser))
                .thenReturn(expectedResponse);

        // Act
        UserResponse actualResponse = userService.save(request);

        // Assert
        assertEquals(expectedResponse, actualResponse);
        verify(teamRepository).findById(teamId);
        verify(userMapper).toEntity(request, team);
        verify(userRepository).save(userToSave);
        verify(userMapper).toResponse(savedUser);
    }

    @Test
    void save_whenTeamNotFound_throwsResourceNotFoundException() {
        // Arrange
        UUID missingTeamId = UUID.randomUUID();

        UserCreateRequest request =
                new UserCreateRequest("Alice", "Engineering", missingTeamId);

        when(teamRepository.findById(missingTeamId))
                .thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> userService.save(request))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Team not found");

        verify(teamRepository).findById(missingTeamId);
        verifyNoInteractions(userRepository);
        verifyNoInteractions(userMapper);
    }

    @Test
    void deleteById_whenUserExists_deletesUser() {
        // Arrange
        UUID userId = UUID.randomUUID();

        when(userRepository.existsById(userId))
                .thenReturn(true);

        // Act
        userService.deleteById(userId);

        // Assert
        verify(userRepository).existsById(userId);
        verify(userRepository).deleteById(userId);
    }

    @Test
    void deleteById_whenUserNotFound_throwsResourceNotFoundException() {
        // Arrange
        UUID userId = UUID.randomUUID();

        when(userRepository.existsById(userId))
                .thenReturn(false);

        // Act & Assert
        assertThatThrownBy(() -> userService.deleteById(userId))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("User not found: " + userId);

        verify(userRepository).existsById(userId);
        verify(userRepository, never()).deleteById(userId);
    }

    @Test
    void findAll_whenNoUsersExist_returnsEmptyList() {
        // Arrange
        when(userRepository.findAll())
                .thenReturn(List.of());

        // Act
        List<UserResponse> actualResponses = userService.findAll();

        // Assert
        assertEquals(List.of(), actualResponses);
        verify(userRepository).findAll();
        verifyNoInteractions(userMapper);
    }
}
