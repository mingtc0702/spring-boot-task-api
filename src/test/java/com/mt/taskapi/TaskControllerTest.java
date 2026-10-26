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
class TaskControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private TaskRepository taskRepository;

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
        return projectRepository.save(new Project(name, owner));
    }

    private Task createTestTask(Project project) {
        Task task = new Task(
                "Original Task",
                "Original Description",
                TaskStatus.TODO,
                project
        );

        return taskRepository.save(task);
    }

    // =========================
    // Validation Tests
    // =========================

    @Test
    void shouldReturnBadRequestWhenTitleIsBlank() throws Exception {

        User alice = createTestUser(
                "Alice",
                "alice.task.validation@example.com"
        );

        Project project = createTestProject("Alice Project", alice);

        String invalidTask = """
                {
                    "title": "",
                    "description": "test",
                    "status": "TODO"
                }
                """;

        mockMvc.perform(
                        post("/api/projects/{projectId}/tasks", project.getId())
                                .with(httpBasic(alice.getEmail(), PASSWORD))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(invalidTask)
                )
                .andExpect(status().isBadRequest());
    }

    // =========================
    // Task Retrieval Tests
    // =========================

    @Test
    void shouldReturnNotFoundWhenTaskDoesNotExist() throws Exception {

        User alice = createTestUser(
                "Alice",
                "alice.task.notfound@example.com"
        );

        mockMvc.perform(
                        get("/api/tasks/999999999")
                                .with(httpBasic(alice.getEmail(), PASSWORD))
                )
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldAllowOwnerToReadTask() throws Exception {

        User alice = createTestUser(
                "Alice",
                "alice.task.read@example.com"
        );

        Project project = createTestProject("Alice Project", alice);
        Task task = createTestTask(project);

        mockMvc.perform(
                        get("/api/tasks/{taskId}", task.getId())
                                .with(httpBasic(alice.getEmail(), PASSWORD))
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(task.getId()))
                .andExpect(jsonPath("$.title").value("Original Task"));
    }

    // =========================
    // Day 11 Ownership Tests
    // =========================

    @Test
    void shouldNotAllowUserToReadAnotherUsersTask() throws Exception {

        User alice = createTestUser(
                "Alice",
                "alice.task.ownership.read@example.com"
        );

        User bob = createTestUser(
                "Bob",
                "bob.task.ownership.read@example.com"
        );

        Project bobProject = createTestProject("Bob Project", bob);
        Task bobTask = createTestTask(bobProject);

        mockMvc.perform(
                        get("/api/tasks/{taskId}", bobTask.getId())
                                .with(httpBasic(alice.getEmail(), PASSWORD))
                )
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldNotAllowUserToUpdateAnotherUsersTask() throws Exception {

        User alice = createTestUser(
                "Alice",
                "alice.task.ownership.update@example.com"
        );

        User bob = createTestUser(
                "Bob",
                "bob.task.ownership.update@example.com"
        );

        Project bobProject = createTestProject("Bob Project", bob);
        Task bobTask = createTestTask(bobProject);

        String updateJson = """
                {
                    "title": "Hacked Task",
                    "description": "Unauthorized Update",
                    "status": "DONE"
                }
                """;

        mockMvc.perform(
                        put("/api/tasks/{taskId}", bobTask.getId())
                                .with(httpBasic(alice.getEmail(), PASSWORD))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(updateJson)
                )
                .andExpect(status().isNotFound());

        Task unchangedTask = taskRepository
                .findById(bobTask.getId())
                .orElseThrow();

        assertEquals("Original Task", unchangedTask.getTitle());
        assertEquals(TaskStatus.TODO, unchangedTask.getStatus());
    }

    @Test
    void shouldNotAllowUserToDeleteAnotherUsersTask() throws Exception {

        User alice = createTestUser(
                "Alice",
                "alice.task.ownership.delete@example.com"
        );

        User bob = createTestUser(
                "Bob",
                "bob.task.ownership.delete@example.com"
        );

        Project bobProject = createTestProject("Bob Project", bob);
        Task bobTask = createTestTask(bobProject);

        mockMvc.perform(
                        delete("/api/tasks/{taskId}", bobTask.getId())
                                .with(httpBasic(alice.getEmail(), PASSWORD))
                )
                .andExpect(status().isNotFound());

        assertTrue(taskRepository.existsById(bobTask.getId()));
    }
}
