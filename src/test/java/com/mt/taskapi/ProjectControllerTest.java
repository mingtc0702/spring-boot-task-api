package com.mt.taskapi;

import com.mt.taskapi.model.Project;
import com.mt.taskapi.model.Task;
import com.mt.taskapi.model.TaskStatus;
import com.mt.taskapi.model.User;

import com.mt.taskapi.repository.ProjectRepository;
import com.mt.taskapi.repository.TaskRepository;
import com.mt.taskapi.repository.UserRepository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class ProjectControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private static final String PASSWORD = "Password123!";

    // =========================
    // Test Helpers
    // =========================

    private User createTestUser(String name, String email) {
        return userRepository.save(
                new User(
                        name,
                        email,
                        passwordEncoder.encode(PASSWORD)
                )
        );
    }

    private Project createTestProject(String name, User owner) {
        return projectRepository.save(
                new Project(name, owner)
        );
    }

    private Task createTestTask(Project project) {
        return taskRepository.save(
                new Task(
                        "Test DTO Response",
                        "Verify response fields",
                        TaskStatus.TODO,
                        project
                )
        );
    }

    // =========================
    // Existing Tests
    // =========================

    @Test
    void shouldReturn404WhenProjectDoesNotExist() throws Exception {

        User alice = createTestUser(
                "Alice",
                "alice.project.notfound@example.com"
        );

        mockMvc.perform(
                        get("/api/projects/999999999")
                                .with(httpBasic(alice.getEmail(), PASSWORD))
                )
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldCreateTaskForProject() throws Exception {

        User alice = createTestUser(
                "Alice",
                "alice.project.create.task@example.com"
        );

        Project project = createTestProject(
                "MockMvc Test Project",
                alice
        );

        mockMvc.perform(
                        post("/api/projects/{projectId}/tasks", project.getId())
                                .with(httpBasic(alice.getEmail(), PASSWORD))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {
                                            "title": "Test MockMvc",
                                            "description": "Testing POST endpoint",
                                            "status": "TODO"
                                        }
                                        """)
                )
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title").value("Test MockMvc"))
                .andExpect(jsonPath("$.status").value("TODO"))
                .andExpect(jsonPath("$.id").isNumber());
    }

    @Test
    void shouldRejectTaskWithBlankTitle() throws Exception {

        User alice = createTestUser(
                "Alice",
                "alice.project.validation.task@example.com"
        );

        Project project = createTestProject(
                "Validation Test Project",
                alice
        );

        mockMvc.perform(
                        post("/api/projects/{projectId}/tasks", project.getId())
                                .with(httpBasic(alice.getEmail(), PASSWORD))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {
                                            "title": "",
                                            "description": "Invalid task",
                                            "status": "TODO"
                                        }
                                        """)
                )
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturnTaskResponseWithoutProject() throws Exception {

        User alice = createTestUser(
                "Alice",
                "alice.project.dto@example.com"
        );

        Project project = createTestProject(
                "DTO Test Project",
                alice
        );

        Task task = createTestTask(project);

        mockMvc.perform(
                        get("/api/tasks/{taskId}", task.getId())
                                .with(httpBasic(alice.getEmail(), PASSWORD))
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(task.getId()))
                .andExpect(jsonPath("$.title").value("Test DTO Response"))
                .andExpect(jsonPath("$.status").value("TODO"))
                .andExpect(jsonPath("$.project").doesNotExist());
    }

    @Test
    void shouldRejectProjectWithBlankName() throws Exception {

        User alice = createTestUser(
                "Alice",
                "alice.project.validation.name@example.com"
        );

        mockMvc.perform(
                        post("/api/projects")
                                .with(httpBasic(alice.getEmail(), PASSWORD))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {
                                            "name": ""
                                        }
                                        """)
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message")
                        .value("name: must not be blank"));
    }

    // =========================
    // Day 11 Ownership Tests
    // =========================

    @Test
    void shouldAllowOwnerToReadProject() throws Exception {

        User alice = createTestUser(
                "Alice",
                "alice.project.owner.read@example.com"
        );

        Project project = createTestProject(
                "Alice Private Project",
                alice
        );

        mockMvc.perform(
                        get("/api/projects/{projectId}", project.getId())
                                .with(httpBasic(alice.getEmail(), PASSWORD))
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(project.getId()))
                .andExpect(jsonPath("$.name")
                        .value("Alice Private Project"));
    }

    @Test
    void shouldNotAllowUserToReadAnotherUsersProject()
            throws Exception {

        User alice = createTestUser(
                "Alice",
                "alice.project.ownership.read@example.com"
        );

        User bob = createTestUser(
                "Bob",
                "bob.project.ownership.read@example.com"
        );

        Project bobProject = createTestProject(
                "Bob Private Project",
                bob
        );

        mockMvc.perform(
                        get("/api/projects/{projectId}", bobProject.getId())
                                .with(httpBasic(alice.getEmail(), PASSWORD))
                )
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldNotAllowUserToAddTaskToAnotherUsersProject()
            throws Exception {

        User alice = createTestUser(
                "Alice",
                "alice.project.ownership.add@example.com"
        );

        User bob = createTestUser(
                "Bob",
                "bob.project.ownership.add@example.com"
        );

        Project bobProject = createTestProject(
                "Bob Private Project",
                bob
        );

        long tasksBefore = taskRepository.count();

        mockMvc.perform(
                        post("/api/projects/{projectId}/tasks", bobProject.getId())
                                .with(httpBasic(alice.getEmail(), PASSWORD))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {
                                            "title": "Unauthorized Task",
                                            "description": "Should not be created",
                                            "status": "TODO"
                                        }
                                        """)
                )
                .andExpect(status().isNotFound());

        assertEquals(tasksBefore, taskRepository.count());
    }

    @Test
    void shouldNotAllowUserToReadAnotherProjectsTasks()
            throws Exception {

        User alice = createTestUser(
                "Alice",
                "alice.project.ownership.tasks@example.com"
        );

        User bob = createTestUser(
                "Bob",
                "bob.project.ownership.tasks@example.com"
        );

        Project bobProject = createTestProject(
                "Bob Private Project",
                bob
        );

        createTestTask(bobProject);

        mockMvc.perform(
                        get("/api/projects/{projectId}/tasks", bobProject.getId())
                                .with(httpBasic(alice.getEmail(), PASSWORD))
                )
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldReturnOnlyProjectsOwnedByCurrentUser()
            throws Exception {

        User alice = createTestUser(
                "Alice",
                "alice.project.filter@example.com"
        );

        User bob = createTestUser(
                "Bob",
                "bob.project.filter@example.com"
        );

        createTestProject("Alice Project A", alice);
        createTestProject("Alice Project B", alice);
        createTestProject("Bob Project C", bob);

        mockMvc.perform(
                        get("/api/projects")
                                .with(httpBasic(alice.getEmail(), PASSWORD))
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath(
                        "$[?(@.name == 'Alice Project A')]"
                ).exists())
                .andExpect(jsonPath(
                        "$[?(@.name == 'Alice Project B')]"
                ).exists())
                .andExpect(jsonPath(
                        "$[?(@.name == 'Bob Project C')]"
                ).doesNotExist());
    }
}
