package com.example.taskmanager.mapper;

import com.example.taskmanager.dto.request.TaskCreateRequest;
import com.example.taskmanager.dto.response.TaskResponse;
import com.example.taskmanager.model.Project;
import com.example.taskmanager.model.Task;
import com.example.taskmanager.model.User;
import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
public class TaskMapper {
    public Task toEntity(TaskCreateRequest request, Project project, Set<User> assignees) {
        Task task = new Task();
        task.setTitle(request.title());
        task.setDescription(request.description());
        task.setProject(project);
        assignees.forEach(task::addAssignee);
        return task;
    }
    public TaskResponse toResponse(Task task) {
        Set<User> users = task.getAssignees();
        Set<UUID> userIds = users.stream()
                .map(User::getId)
                .collect(Collectors.toSet());

        UUID projectId = task.getProject() == null
                ? null
                : task.getProject().getId();

        return new TaskResponse(
                task.getId(),
                task.getTitle(),
                task.getDescription(),
                task.getStatus(),
                projectId,
                userIds
        );
    }
}
