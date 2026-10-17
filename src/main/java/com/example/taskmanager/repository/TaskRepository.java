package com.example.taskmanager.repository;

import com.example.taskmanager.model.Task;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface TaskRepository extends JpaRepository<Task, UUID> {

    @EntityGraph(attributePaths = {"assignees", "project"})
    @Query("SELECT DISTINCT t FROM Task t")
    List<Task> findAllWithAssigneesAndProject();

    @EntityGraph(attributePaths = {"assignees", "project"})
    @Query("SELECT t FROM Task t WHERE t.id = :id")
    Optional<Task> findByIdWithAssigneesAndProject(@Param("id") UUID id);
}