package com.example.taskmanager.service;

import com.example.taskmanager.dto.TaskCreateRequest;
import com.example.taskmanager.dto.TaskResponse;
import com.example.taskmanager.exception.ResourceNotFoundException;
import com.example.taskmanager.mapper.TaskMapper;
import com.example.taskmanager.model.Project;
import com.example.taskmanager.model.Task;
import com.example.taskmanager.model.User;
import com.example.taskmanager.repository.ProjectRepository;
import com.example.taskmanager.repository.TaskRepository;
import com.example.taskmanager.repository.UserRepository;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class TaskService {

    private final TaskRepository taskRepository;
    private final TaskMapper taskMapper;
    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;

    public TaskService(TaskRepository taskRepository, TaskMapper taskMapper, ProjectRepository projectRepository, UserRepository userRepository) {
        this.taskRepository = taskRepository;
        this.taskMapper = taskMapper;
        this.projectRepository = projectRepository;
        this.userRepository = userRepository;
    }


    @Transactional(readOnly = true)
    public List<TaskResponse> findAll() {
        return taskRepository.findAllWithAssigneesAndProject()
                .stream()
                .map(taskMapper::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public TaskResponse findById(UUID id) {
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found: " + id));
        return taskMapper.toResponse(task);
    }

    @Transactional
    public TaskResponse save(TaskCreateRequest request) {
        Project project = resolveProject(request.projectId());
        Set<User> users = resolveAssignees(request.assigneeIds());

        Task task = taskMapper.toEntity(request, project, users);
        Task savedTask = taskRepository.save(task);

        return taskMapper.toResponse(savedTask);
    }

    @Transactional
    public void deleteById(UUID id) {
        Task task = taskRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Task not found: " + id));

        taskRepository.delete(task);
    }

    private Project resolveProject(UUID projectId) {
        return projectId == null
                ? null
                : projectRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found: " + projectId));
    }

    private Set<User> resolveAssignees(Set<UUID> assigneeIds) {
        if (assigneeIds == null || assigneeIds.isEmpty()) {
            return new HashSet<>();
        }
        List<User> users = userRepository.findAllById(assigneeIds);

        Set<UUID> foundIds = users.stream()
                .map(User::getId)
                .collect(Collectors.toSet());

        if (!foundIds.equals(assigneeIds)) {
            throw new ResourceNotFoundException("One or more users not found");
        }

        return new HashSet<>(users);
    }
}
