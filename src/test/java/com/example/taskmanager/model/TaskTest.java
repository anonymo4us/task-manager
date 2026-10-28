package com.example.taskmanager.model;

import com.example.taskmanager.exception.InvalidTaskStatusTransitionException;
import com.example.taskmanager.status.TaskStatus;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TaskTest {

    @Test
    void changeStatus_todoToInProgress_changesStatus() {
        // Arrange
        Task task = new Task();

        // Act
        task.changeStatus(TaskStatus.IN_PROGRESS);

        // Assert
        assertThat(task.getStatus()).isEqualTo(TaskStatus.IN_PROGRESS);
    }

    @Test
    void changeStatus_todoToDone_throwsAndKeepsStatus() {
        // Arrange
        Task task = new Task();

        // Act + Assert
        assertThatThrownBy(() -> task.changeStatus(TaskStatus.DONE))
                .isInstanceOf(InvalidTaskStatusTransitionException.class)
                .hasMessageContaining("TODO")
                .hasMessageContaining("DONE");

        assertThat(task.getStatus()).isEqualTo(TaskStatus.TODO);
    }

    @Test
    void changeStatus_null_throwsAndKeepsStatus() {
        // Arrange
        Task task = new Task();

        // Act + Assert
        assertThatThrownBy(() -> task.changeStatus(null))
                .isInstanceOf(NullPointerException.class)
                .hasMessage("New status must not be null");

        assertThat(task.getStatus()).isEqualTo(TaskStatus.TODO);
    }

    @Test
    void changeStatus_inProgressToDone_changesStatus() {
        // Arrange
        Task task = new Task();
        task.changeStatus(TaskStatus.IN_PROGRESS);

        // Act
        task.changeStatus(TaskStatus.DONE);

        // Assert
        assertThat(task.getStatus()).isEqualTo(TaskStatus.DONE);
    }

    @Test
    void changeStatus_doneToInProgress_changesStatus() {
        // Arrange: доводим задачу до DONE через легальные переходы
        Task task = new Task();
        task.changeStatus(TaskStatus.IN_PROGRESS);
        task.changeStatus(TaskStatus.DONE);

        // Act
        task.changeStatus(TaskStatus.IN_PROGRESS);

        // Assert
        assertThat(task.getStatus()).isEqualTo(TaskStatus.IN_PROGRESS);
    }

    @Test
    void changeStatus_inProgressToTodo_throwsAndKeepsStatus() {
        // Arrange
        Task task = new Task();
        task.changeStatus(TaskStatus.IN_PROGRESS);

        // Act + Assert
        assertThatThrownBy(() -> task.changeStatus(TaskStatus.TODO))
                .isInstanceOf(InvalidTaskStatusTransitionException.class)
                .hasMessageContaining("IN_PROGRESS")
                .hasMessageContaining("TODO");

        assertThat(task.getStatus()).isEqualTo(TaskStatus.IN_PROGRESS);
    }

    @Test
    void changeStatus_sameStatus_throwsAndKeepsStatus() {
        // Arrange
        Task task = new Task();

        // Act + Assert
        assertThatThrownBy(() -> task.changeStatus(TaskStatus.TODO))
                .isInstanceOf(InvalidTaskStatusTransitionException.class);

        assertThat(task.getStatus()).isEqualTo(TaskStatus.TODO);
    }
}