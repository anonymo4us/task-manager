package com.example.taskmanager.specification;

import com.example.taskmanager.model.Task;
import com.example.taskmanager.status.TaskStatus;
import org.springframework.data.jpa.domain.Specification;

import java.util.UUID;

public class TaskSpecification {

    public static Specification<Task> hasStatus(TaskStatus status) {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.equal(root.get("status"), status);
    }

    public static Specification<Task> hasProjectId(UUID projectId) {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.equal(root.get("project").get("id"), projectId);
    }

}
