package com.example.taskmanager.model;

import com.example.taskmanager.exception.InvalidTaskStatusTransitionException;
import com.example.taskmanager.status.TaskStatus;
import jakarta.persistence.*;
import org.hibernate.annotations.UuidGenerator;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "tasks")
public class Task {

    @Id
    @UuidGenerator
    private UUID id;

    @Column(nullable = false)
    private String title;

    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TaskStatus status = TaskStatus.TODO;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id")
    private Project project;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "task_assignees",
            joinColumns = @JoinColumn(name = "task_id"),
            inverseJoinColumns = @JoinColumn(name = "user_id")
    )
    private Set<User> assignees = new HashSet<>();

    public UUID getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public TaskStatus getStatus() {
        return status;
    }

    public void changeStatus(TaskStatus newStatus) {
        Objects.requireNonNull(newStatus, "New status must not be null");
        if (!canTransitionTo(newStatus)){
            throw new InvalidTaskStatusTransitionException("Cannot change status from " + status + " to " + newStatus);
        }

        this.status = newStatus;
    }

    private boolean canTransitionTo(TaskStatus newStatus) {
        return switch (status) {
            case TODO -> newStatus == TaskStatus.IN_PROGRESS;
            case IN_PROGRESS -> newStatus == TaskStatus.DONE;
            case DONE -> newStatus == TaskStatus.IN_PROGRESS;
        };
    }

    public Project getProject() {
        return project;
    }

    public void setProject(Project project) {
        this.project = project;
    }

    public Set<User> getAssignees() {
        return assignees;
    }

    public void addAssignee(User user) {
        assignees.add(Objects.requireNonNull(user));
    }

    public void removeAssignee(User user) {
        assignees.remove(user);
    }
}