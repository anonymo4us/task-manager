package com.example.taskmanager.service;

import com.example.taskmanager.dto.request.TaskCreateRequest;
import com.example.taskmanager.dto.response.TaskResponse;
import com.example.taskmanager.exception.ResourceNotFoundException;
import com.example.taskmanager.mapper.TaskMapper;
import com.example.taskmanager.model.Project;
import com.example.taskmanager.model.Task;
import com.example.taskmanager.model.User;
import com.example.taskmanager.repository.ProjectRepository;
import com.example.taskmanager.repository.TaskRepository;
import com.example.taskmanager.repository.UserRepository;
import com.example.taskmanager.status.TaskStatus;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.jpa.domain.Specification;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TaskServiceTest {

    @Mock
    private TaskRepository taskRepository;

    @Mock
    private TaskMapper taskMapper;

    @Mock
    private ProjectRepository projectRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private TaskService taskService;

    @Test
    void findById_whenTaskExists_returnsTaskResponse() {
        // Arrange
        UUID taskId = UUID.randomUUID();
        UUID projectId = UUID.randomUUID();
        UUID assigneeId = UUID.randomUUID();

        Project project = new Project();
        project.setId(projectId);

        User assignee = new User();
        assignee.setId(assigneeId);

        Task task = new Task();
        task.setTitle("Fix bug");
        task.setDescription("Critical bug in production");
        task.setProject(project);
        task.addAssignee(assignee);

        TaskResponse expectedResponse = new TaskResponse(
                taskId,
                "Fix bug",
                "Critical bug in production",
                TaskStatus.TODO,
                projectId,
                Set.of(assigneeId)
        );

        when(taskRepository.findById(taskId)).thenReturn(Optional.of(task));
        when(taskMapper.toResponse(task)).thenReturn(expectedResponse);

        // Act
        TaskResponse actualResponse = taskService.findById(taskId);

        // Assert
        assertEquals(expectedResponse, actualResponse);
        verify(taskRepository).findById(taskId);
        verify(taskMapper).toResponse(task);
    }

    @Test
    void findById_whenTaskNotFound_throwsResourceNotFoundException() {
        // Arrange
        UUID taskId = UUID.randomUUID();

        when(taskRepository.findById(taskId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> taskService.findById(taskId))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Task not found: " + taskId);

        verify(taskRepository).findById(taskId);
        verifyNoInteractions(taskMapper);
    }

    @Test
    void findAll_whenTasksExist_returnsListOfTaskResponses() {
        // Arrange
        UUID firstTaskId = UUID.randomUUID();
        UUID secondTaskId = UUID.randomUUID();
        UUID projectId = UUID.randomUUID();
        UUID assigneeId = UUID.randomUUID();

        Project project = new Project();
        project.setId(projectId);

        User assignee = new User();
        assignee.setId(assigneeId);

        Task firstTask = new Task();
        firstTask.setTitle("Fix bug");
        firstTask.setProject(project);
        firstTask.addAssignee(assignee);

        Task secondTask = new Task();
        secondTask.setTitle("Write tests");
        secondTask.setProject(null);

        TaskResponse firstResponse = new TaskResponse(
                firstTaskId, "Fix bug", null, TaskStatus.TODO, projectId, Set.of(assigneeId)
        );
        TaskResponse secondResponse = new TaskResponse(
                secondTaskId, "Write tests", null, TaskStatus.TODO, null, Set.of()
        );

        List<TaskResponse> expectedResponses = List.of(firstResponse, secondResponse);

        when(taskRepository.findAllWithAssigneesAndProject()).thenReturn(List.of(firstTask, secondTask));
        when(taskMapper.toResponse(firstTask)).thenReturn(firstResponse);
        when(taskMapper.toResponse(secondTask)).thenReturn(secondResponse);

        // Act
        List<TaskResponse> actualResponses = taskService.findAll();

        // Assert
        assertEquals(expectedResponses, actualResponses);
        verify(taskRepository).findAllWithAssigneesAndProject();
        verify(taskMapper).toResponse(firstTask);
        verify(taskMapper).toResponse(secondTask);
    }

    @Test
    void findAll_whenNoTasksExist_returnsEmptyList() {
        // Arrange
        when(taskRepository.findAllWithAssigneesAndProject()).thenReturn(List.of());

        // Act
        List<TaskResponse> actualResponses = taskService.findAll();

        // Assert
        assertTrue(actualResponses.isEmpty());
        verify(taskRepository).findAllWithAssigneesAndProject();
        verifyNoInteractions(taskMapper);
    }

    @Test
    void save_whenProjectIdIsNull_createsTaskWithoutProject() {
        // Arrange
        TaskCreateRequest request = new TaskCreateRequest(
                "Fix bug",
                "Critical bug",
                null,
                null
        );

        Task taskToSave = new Task();
        taskToSave.setTitle("Fix bug");
        taskToSave.setDescription("Critical bug");
        taskToSave.setProject(null);

        UUID savedTaskId = UUID.randomUUID();
        Task savedTask = new Task();
        savedTask.setTitle("Fix bug");
        savedTask.setDescription("Critical bug");
        savedTask.setProject(null);

        TaskResponse expectedResponse = new TaskResponse(
                savedTaskId, "Fix bug", "Critical bug", TaskStatus.TODO, null, Set.of()
        );

        when(taskMapper.toEntity(request, null, Set.of())).thenReturn(taskToSave);
        when(taskRepository.save(taskToSave)).thenReturn(savedTask);
        when(taskMapper.toResponse(savedTask)).thenReturn(expectedResponse);

        // Act
        TaskResponse actualResponse = taskService.save(request);

        // Assert
        assertEquals(expectedResponse, actualResponse);
        verify(taskMapper).toEntity(request, null, Set.of());
        verify(taskRepository).save(taskToSave);
        verify(taskMapper).toResponse(savedTask);

        verifyNoInteractions(projectRepository);
        verifyNoInteractions(userRepository);
    }

    @Test
    void save_whenProjectAndAssigneesExist_createsTask() {
        // Arrange
        UUID projectId = UUID.randomUUID();
        UUID firstUserId = UUID.randomUUID();
        UUID secondUserId = UUID.randomUUID();

        Project project = new Project();
        project.setId(projectId);
        project.setName("Task Manager");

        User firstUser = new User();
        firstUser.setId(firstUserId);
        firstUser.setName("Alice");

        User secondUser = new User();
        secondUser.setId(secondUserId);
        secondUser.setName("Bob");

        TaskCreateRequest request = new TaskCreateRequest(
                "Fix bug",
                "Critical bug",
                projectId,
                Set.of(firstUserId, secondUserId)
        );

        Set<User> assignees = new HashSet<>(Set.of(firstUser, secondUser));

        Task taskToSave = new Task();
        taskToSave.setTitle("Fix bug");
        taskToSave.setDescription("Critical bug");
        taskToSave.setProject(project);
        assignees.forEach(taskToSave::addAssignee);

        UUID savedTaskId = UUID.randomUUID();
        Task savedTask = new Task();
        savedTask.setTitle("Fix bug");
        savedTask.setDescription("Critical bug");
        savedTask.setProject(project);
        assignees.forEach(savedTask::addAssignee);

        TaskResponse expectedResponse = new TaskResponse(
                savedTaskId,
                "Fix bug",
                "Critical bug",
                TaskStatus.TODO,
                projectId,
                Set.of(firstUserId, secondUserId)
        );

        when(projectRepository.findById(projectId)).thenReturn(Optional.of(project));
        when(userRepository.findAllById(request.assigneeIds()))
                .thenReturn(List.of(firstUser, secondUser));
        when(taskMapper.toEntity(request, project, assignees)).thenReturn(taskToSave);
        when(taskRepository.save(taskToSave)).thenReturn(savedTask);
        when(taskMapper.toResponse(savedTask)).thenReturn(expectedResponse);

        // Act
        TaskResponse actualResponse = taskService.save(request);

        // Assert
        assertEquals(expectedResponse, actualResponse);

        verify(projectRepository).findById(projectId);
        verify(userRepository).findAllById(request.assigneeIds());
        verify(taskMapper).toEntity(request, project, assignees);
        verify(taskRepository).save(taskToSave);
        verify(taskMapper).toResponse(savedTask);
    }

    @Test
    void save_whenProjectExistsAndAssigneesEmpty_createsTaskWithoutAssignees() {
        // Arrange
        UUID projectId = UUID.randomUUID();

        Project project = new Project();
        project.setId(projectId);
        project.setName("Task Manager");

        TaskCreateRequest request = new TaskCreateRequest(
                "Fix bug",
                "Critical bug",
                projectId,
                Set.of()
        );

        Task taskToSave = new Task();
        taskToSave.setTitle("Fix bug");
        taskToSave.setDescription("Critical bug");
        taskToSave.setProject(project);

        UUID savedTaskId = UUID.randomUUID();
        Task savedTask = new Task();
        savedTask.setTitle("Fix bug");
        savedTask.setDescription("Critical bug");
        savedTask.setProject(project);

        TaskResponse expectedResponse = new TaskResponse(
                savedTaskId,
                "Fix bug",
                "Critical bug",
                TaskStatus.TODO,
                projectId,
                Set.of()
        );

        when(projectRepository.findById(projectId)).thenReturn(Optional.of(project));
        when(taskMapper.toEntity(request, project, Set.of())).thenReturn(taskToSave);
        when(taskRepository.save(taskToSave)).thenReturn(savedTask);
        when(taskMapper.toResponse(savedTask)).thenReturn(expectedResponse);

        // Act
        TaskResponse actualResponse = taskService.save(request);

        // Assert
        assertEquals(expectedResponse, actualResponse);

        verify(projectRepository).findById(projectId);
        verify(taskMapper).toEntity(request, project, Set.of());
        verify(taskRepository).save(taskToSave);
        verify(taskMapper).toResponse(savedTask);

        verifyNoInteractions(userRepository);
    }

    @Test
    void save_whenProjectNotFound_throwsResourceNotFoundException() {
        // Arrange
        UUID missingProjectId = UUID.randomUUID();

        TaskCreateRequest request = new TaskCreateRequest(
                "Fix bug",
                "Critical bug",
                missingProjectId,
                Set.of()
        );

        when(projectRepository.findById(missingProjectId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> taskService.save(request))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Project not found: " + missingProjectId);

        verify(projectRepository).findById(missingProjectId);
        verifyNoInteractions(taskMapper);
        verifyNoInteractions(taskRepository);
        verifyNoInteractions(userRepository);
    }

    @Test
    void save_whenAssigneeNotFound_throwsResourceNotFoundException() {
        // Arrange
        UUID projectId = UUID.randomUUID();
        UUID existingUserId = UUID.randomUUID();
        UUID missingUserId = UUID.randomUUID();

        Project project = new Project();
        project.setId(projectId);

        User existingUser = new User();
        existingUser.setId(existingUserId);

        TaskCreateRequest request = new TaskCreateRequest(
                "Fix bug",
                "Critical bug",
                projectId,
                Set.of(existingUserId, missingUserId)
        );

        when(projectRepository.findById(projectId))
                .thenReturn(Optional.of(project));
        when(userRepository.findAllById(request.assigneeIds()))
                .thenReturn(List.of(existingUser));

        // Act & Assert
        assertThatThrownBy(() -> taskService.save(request))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("One or more users not found");

        verify(projectRepository).findById(projectId);
        verify(userRepository).findAllById(request.assigneeIds());
        verifyNoInteractions(taskMapper);
        verifyNoInteractions(taskRepository);
    }

    @Test
    void save_whenAssigneeIdsDifferFromFoundIds_throwsResourceNotFoundException() {
        // Arrange
        UUID projectId = UUID.randomUUID();
        UUID requestedUserId = UUID.randomUUID();
        UUID wrongUserId = UUID.randomUUID();

        Project project = new Project();
        project.setId(projectId);

        User wrongUser = new User();
        wrongUser.setId(wrongUserId);

        TaskCreateRequest request = new TaskCreateRequest(
                "Fix bug",
                "Critical bug",
                projectId,
                Set.of(requestedUserId)
        );

        when(projectRepository.findById(projectId)).thenReturn(Optional.of(project));
        when(userRepository.findAllById(request.assigneeIds()))
                .thenReturn(List.of(wrongUser));

        // Act & Assert
        assertThatThrownBy(() -> taskService.save(request))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("One or more users not found");

        verify(projectRepository).findById(projectId);
        verify(userRepository).findAllById(request.assigneeIds());
        verifyNoInteractions(taskMapper);
        verifyNoInteractions(taskRepository);
    }

    @Test
    void deleteById_whenTaskExists_deletesTask() {
        // Arrange
        UUID taskId = UUID.randomUUID();
        Task task = new Task();
        task.setTitle("Fix bug");

        when(taskRepository.findById(taskId)).thenReturn(Optional.of(task));

        // Act
        taskService.deleteById(taskId);

        // Assert
        verify(taskRepository).findById(taskId);
        verify(taskRepository).delete(task);
    }

    @Test
    void deleteById_whenTaskNotFound_throwsResourceNotFoundException() {
        // Arrange
        UUID taskId = UUID.randomUUID();

        when(taskRepository.findById(taskId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> taskService.deleteById(taskId))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Task not found: " + taskId);

        verify(taskRepository).findById(taskId);
        verify(taskRepository, never()).delete(any(Task.class));
    }

    @Test
    void findAll_withoutFilters_shouldReturnAllTasks() {
        // Arrange
        UUID projectId = UUID.randomUUID();
        UUID assigneeId = UUID.randomUUID();

        Project project = new Project();
        project.setId(projectId);

        User assignee = new User();
        assignee.setId(assigneeId);

        Task firstTask = new Task();
        firstTask.setTitle("Fix bug");
        firstTask.setProject(project);
        firstTask.addAssignee(assignee);

        Task secondTask = new Task();
        secondTask.setTitle("Write tests");

        TaskResponse firstResponse = new TaskResponse(
                UUID.randomUUID(), "Fix bug", null,
                TaskStatus.TODO, projectId, Set.of(assigneeId)
        );
        TaskResponse secondResponse = new TaskResponse(
                UUID.randomUUID(), "Write tests", null,
                TaskStatus.TODO, null, Set.of()
        );

        when(taskRepository.findAll(ArgumentMatchers.<Specification<Task>>any()))
                .thenReturn(List.of(firstTask, secondTask));
        when(taskMapper.toResponse(firstTask)).thenReturn(firstResponse);
        when(taskMapper.toResponse(secondTask)).thenReturn(secondResponse);

        // Act
        List<TaskResponse> actualResponses = taskService.findAll(null, null);

        // Assert
        assertEquals(List.of(firstResponse, secondResponse), actualResponses);
        verify(taskRepository).findAll(ArgumentMatchers.<Specification<Task>>any());
        verify(taskMapper).toResponse(firstTask);
        verify(taskMapper).toResponse(secondTask);
    }

    @Test
    void findAll_withStatusFilter_shouldReturnTasksWithMatchingStatus() {
        // Arrange
        UUID projectId = UUID.randomUUID();
        UUID assigneeId = UUID.randomUUID();

        Project project = new Project();
        project.setId(projectId);

        User assignee = new User();
        assignee.setId(assigneeId);

        Task matchingTask = new Task();
        matchingTask.setTitle("In progress task");
        matchingTask.setProject(project);
        matchingTask.addAssignee(assignee);
        matchingTask.changeStatus(TaskStatus.IN_PROGRESS);

        Task nonMatchingTask = new Task();
        nonMatchingTask.setTitle("Todo task");
        nonMatchingTask.setProject(null);

        TaskResponse expectedResponse = new TaskResponse(
                UUID.randomUUID(),
                "In progress task",
                null,
                TaskStatus.IN_PROGRESS,
                projectId,
                Set.of(assigneeId)
        );

        when(taskRepository.findAll(ArgumentMatchers.<Specification<Task>>any()))
                .thenReturn(List.of(matchingTask));
        when(taskMapper.toResponse(matchingTask)).thenReturn(expectedResponse);

        // Act
        List<TaskResponse> actualResponses = taskService.findAll(TaskStatus.IN_PROGRESS, null);

        // Assert
        assertEquals(List.of(expectedResponse), actualResponses);

        verify(taskRepository).findAll(ArgumentMatchers.<Specification<Task>>any());
        verify(taskMapper).toResponse(matchingTask);
        verifyNoMoreInteractions(taskMapper);
    }

    @Test
    void findAll_withProjectIdFilter_shouldReturnTasksFromMatchingProject() {
        // Arrange
        UUID projectId = UUID.randomUUID();
        UUID assigneeId = UUID.randomUUID();

        Project matchingProject = new Project();
        matchingProject.setId(projectId);
        matchingProject.setName("Matching project");

        Project otherProject = new Project();
        otherProject.setId(UUID.randomUUID());
        otherProject.setName("Other project");

        User assignee = new User();
        assignee.setId(assigneeId);

        Task matchingTask = new Task();
        matchingTask.setTitle("Task from target project");
        matchingTask.setProject(matchingProject);
        matchingTask.addAssignee(assignee);

        Task nonMatchingTask = new Task();
        nonMatchingTask.setTitle("Task from other project");
        nonMatchingTask.setProject(otherProject);

        TaskResponse expectedResponse = new TaskResponse(
                UUID.randomUUID(),
                "Task from target project",
                null,
                TaskStatus.TODO,
                projectId,
                Set.of(assigneeId)
        );

        when(taskRepository.findAll(ArgumentMatchers.<Specification<Task>>any()))
                .thenReturn(List.of(matchingTask));
        when(taskMapper.toResponse(matchingTask)).thenReturn(expectedResponse);

        // Act
        List<TaskResponse> actualResponses = taskService.findAll(null, projectId);

        // Assert
        assertEquals(List.of(expectedResponse), actualResponses);

        verify(taskRepository).findAll(ArgumentMatchers.<Specification<Task>>any());
        verify(taskMapper).toResponse(matchingTask);
        verifyNoMoreInteractions(taskMapper);
    }

    @Test
    void findAll_withStatusAndProjectIdFilters_shouldReturnMatchingTasks() {
        // Arrange
        UUID targetProjectId = UUID.randomUUID();
        UUID otherProjectId = UUID.randomUUID();
        UUID assigneeId = UUID.randomUUID();

        Project targetProject = new Project();
        targetProject.setId(targetProjectId);
        targetProject.setName("Target project");

        Project otherProject = new Project();
        otherProject.setId(otherProjectId);
        otherProject.setName("Other project");

        User assignee = new User();
        assignee.setId(assigneeId);

        Task matchingTask = new Task();
        matchingTask.setTitle("Matching task");
        matchingTask.setProject(targetProject);
        matchingTask.addAssignee(assignee);
        matchingTask.changeStatus(TaskStatus.IN_PROGRESS);

        Task wrongProjectTask = new Task();
        wrongProjectTask.setTitle("In progress in other project");
        wrongProjectTask.setProject(otherProject);
        wrongProjectTask.changeStatus(TaskStatus.IN_PROGRESS);

        Task wrongStatusTask = new Task();
        wrongStatusTask.setTitle("Todo in target project");
        wrongStatusTask.setProject(targetProject);

        Task nonMatchingTask = new Task();
        nonMatchingTask.setTitle("Todo in other project");
        nonMatchingTask.setProject(otherProject);

        TaskResponse expectedResponse = new TaskResponse(
                UUID.randomUUID(),
                "Matching task",
                null,
                TaskStatus.IN_PROGRESS,
                targetProjectId,
                Set.of(assigneeId)
        );

        when(taskRepository.findAll(ArgumentMatchers.<Specification<Task>>any()))
                .thenReturn(List.of(matchingTask));
        when(taskMapper.toResponse(matchingTask)).thenReturn(expectedResponse);

        // Act
        List<TaskResponse> actualResponses =
                taskService.findAll(TaskStatus.IN_PROGRESS, targetProjectId);

        // Assert
        assertEquals(List.of(expectedResponse), actualResponses);

        verify(taskRepository).findAll(ArgumentMatchers.<Specification<Task>>any());
        verify(taskMapper).toResponse(matchingTask);
        verifyNoMoreInteractions(taskMapper);
    }

}
