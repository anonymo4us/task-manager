package com.example.taskmanager.repository;

import com.example.taskmanager.model.Project;
import com.example.taskmanager.model.Task;
import com.example.taskmanager.model.Team;
import com.example.taskmanager.model.User;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.PersistenceUnit;
import org.hibernate.SessionFactory;
import org.hibernate.stat.Statistics;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.containers.PostgreSQLContainer;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest(properties = {
        "spring.jpa.properties.hibernate.generate_statistics=true"
})
@Testcontainers
class TaskRepositoryTest {

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TeamRepository teamRepository;

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private EntityManager entityManager;

    @PersistenceUnit
    private EntityManagerFactory entityManagerFactory;

    @Container
    static PostgreSQLContainer<?> postgres =
            new PostgreSQLContainer<>("postgres:16");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @Test
    void findAllWithAssignees_shouldReturnEmptyListWhenNoTasksExist() {
        List<Task> result = taskRepository.findAllWithAssigneesAndProject();

        assertThat(result).isEmpty();
    }

    @Test
    void findAllWithAssignees_shouldReturnTaskWithBothAssignees() {
        User alice = new User();
        alice.setName("Alice");
        alice.setDepartment("Backend");

        User bob = new User();
        bob.setName("Bob");
        bob.setDepartment("QA");

        User savedAlice = userRepository.save(alice);
        User savedBob = userRepository.save(bob);

        Task task = new Task();
        task.setTitle("Work");
        task.setDescription("Java");
        task.addAssignee(savedAlice);
        task.addAssignee(savedBob);

        Task savedTask = taskRepository.save(task);

        List<Task> result = taskRepository.findAllWithAssigneesAndProject();

        assertThat(result).hasSize(1);

        Task loaded = result.get(0);
        assertThat(loaded.getId()).isEqualTo(savedTask.getId());
        assertThat(loaded.getTitle()).isEqualTo("Work");
        assertThat(loaded.getAssignees())
                .hasSize(2)
                .extracting(User::getId)
                .containsExactlyInAnyOrder(savedAlice.getId(), savedBob.getId());
    }

    @Test
    void findAllWithAssignees_shouldReturnTaskWithoutAssignees() {

        Task task = new Task();
        task.setTitle("WoW");
        Task savedTask = taskRepository.save(task);

        List<Task> result = taskRepository.findAllWithAssigneesAndProject();

        assertThat(result).hasSize(1);
        Task loaded = result.get(0);
        assertThat(loaded.getId()).isEqualTo(savedTask.getId());
        assertThat(loaded.getTitle()).isEqualTo("WoW");
        assertThat(loaded.getAssignees()).isEmpty();
    }

    @Test
    void findAllWithAssignees_shouldReturnMultipleTasksWithoutDuplicates() {

        User alice = new User();
        alice.setName("Alice");
        alice.setDepartment("Backend");

        User bob = new User();
        bob.setName("Bob");
        bob.setDepartment("QA");

        User savedAlice = userRepository.save(alice);
        User savedBob = userRepository.save(bob);

        Task taskWithAssignees = new Task();
        taskWithAssignees.setTitle("Fix N+1");
        taskWithAssignees.setDescription("Uses JOIN FETCH");
        taskWithAssignees.addAssignee(savedAlice);
        taskWithAssignees.addAssignee(savedBob);
        Task savedWithAssignees = taskRepository.save(taskWithAssignees);

        Task taskWithoutAssignees = new Task();
        taskWithoutAssignees.setTitle("Write docs");
        taskWithoutAssignees.setDescription("No assignees yet");

        Task savedWithoutAssignees = taskRepository.save(taskWithoutAssignees);

        List<Task> result = taskRepository.findAllWithAssigneesAndProject();

        assertThat(result).hasSize(2);

        assertThat(result)
                .extracting(Task::getId)
                .containsExactlyInAnyOrder(
                        savedWithAssignees.getId(),
                        savedWithoutAssignees.getId()
                );

        Task loadedWithAssignees = result.stream()
                .filter(t -> t.getId().equals(savedWithAssignees.getId()))
                .findFirst()
                .orElseThrow();

        Task loadedWithoutAssignees = result.stream()
                .filter(t -> t.getId().equals(savedWithoutAssignees.getId()))
                .findFirst()
                .orElseThrow();

        assertThat(loadedWithAssignees.getAssignees())
                .extracting(User::getId)
                .containsExactlyInAnyOrder(savedAlice.getId(), savedBob.getId());

        assertThat(loadedWithoutAssignees.getAssignees()).isEmpty();
    }

    @Test
    void findByIdWithAssigneesAndProject_shouldLoadTaskWithRelations() {
        // Arrange
        Team team = new Team();
        team.setTeamName("Backend");
        team.setSpecialization("Java");
        Team savedTeam = teamRepository.save(team);

        Project project = new Project();
        project.setName("Task Manager");
        project.setTeam(savedTeam);
        Project savedProject = projectRepository.save(project);

        User user = new User();
        user.setName("Alice");
        user.setDepartment("Backend");
        User savedUser = userRepository.save(user);

        Task task = new Task();
        task.setTitle("Fix N+1");
        task.setDescription("Replace lazy loading with JOIN FETCH");
        task.setProject(savedProject);
        task.addAssignee(savedUser);
        Task savedTask = taskRepository.save(task);

        // Act
        Task loaded = taskRepository
                .findByIdWithAssigneesAndProject(savedTask.getId())
                .orElseThrow();

        // Assert: данные задачи
        assertThat(loaded.getId()).isEqualTo(savedTask.getId());
        assertThat(loaded.getTitle()).isEqualTo("Fix N+1");

        // Assert: связи загружены
        var persistenceUnitUtil = entityManagerFactory.getPersistenceUnitUtil();

        assertThat(persistenceUnitUtil.isLoaded(loaded, "project")).isTrue();
        assertThat(persistenceUnitUtil.isLoaded(loaded, "assignees")).isTrue();

        // Assert: данные проекта
        assertThat(loaded.getProject()).isNotNull();
        assertThat(loaded.getProject().getId()).isEqualTo(savedProject.getId());
        assertThat(loaded.getProject().getName()).isEqualTo("Task Manager");

        // Assert: данные исполнителей
        assertThat(loaded.getAssignees())
                .hasSize(1)
                .extracting(User::getId)
                .containsExactly(savedUser.getId());
    }

    @Test
    void findAllWithAssigneesAndProject_shouldExecuteExpectedNumberOfQueries() {
        Team team = new Team();
        team.setTeamName("Backend");
        team.setSpecialization("Java");
        Team savedTeam = teamRepository.save(team);

        Project project = new Project();
        project.setName("Task Manager");
        project.setTeam(savedTeam);
        Project savedProject = projectRepository.save(project);

        User alice = new User();
        alice.setName("Alice");
        alice.setDepartment("Backend");
        User savedAlice = userRepository.save(alice);

        User bob = new User();
        bob.setName("Bob");
        bob.setDepartment("QA");
        User savedBob = userRepository.save(bob);

        for (int i = 0; i < 3; i++) {
            Task task = new Task();
            task.setTitle("Task " + i);
            task.setProject(savedProject);
            task.addAssignee(savedAlice);
            task.addAssignee(savedBob);
            taskRepository.save(task);
        }

        entityManager.flush();
        entityManager.clear();

        Statistics statistics = entityManagerFactory
                .unwrap(SessionFactory.class)
                .getStatistics();
        statistics.clear();

        // Act
        List<Task> result = taskRepository.findAllWithAssigneesAndProject();

        long queryCount = statistics.getPrepareStatementCount();
        System.out.println("SQL queries executed by findAllWithAssigneesAndProject: " + queryCount);

        assertThat(queryCount).isEqualTo(1);
        assertThat(result).hasSize(3);
        assertThat(result).allSatisfy(task -> {
            assertThat(task.getProject()).isNotNull();
            assertThat(task.getProject().getName()).isEqualTo("Task Manager");

            assertThat(task.getAssignees())
                    .hasSize(2)
                    .extracting(User::getName)
                    .containsExactlyInAnyOrder("Alice", "Bob");
        });
        assertThat(statistics.getPrepareStatementCount()).isEqualTo(1);
    }
}