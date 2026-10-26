
package com.mt.taskapi;

import com.mt.taskapi.model.User;
import com.mt.taskapi.model.Project;
import com.mt.taskapi.repository.UserRepository;
import com.mt.taskapi.repository.ProjectRepository;

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
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProjectRepository projectRepository;

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

    // =========================
    // User API Tests
    // =========================

    @Test
    void shouldCreateUser() throws Exception {

        String json = """
                {
                    "name": "Alice",
                    "email": "alice.test@example.com",
                    "password": "Password123!"
                }
                """;

        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Alice"))
                .andExpect(jsonPath("$.email").value("alice.test@example.com"))
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.password").doesNotExist());

        User savedUser = userRepository
                .findByEmail("alice.test@example.com")
                .orElseThrow();

        assertNotEquals("Password123!", savedUser.getPassword());

        assertTrue(passwordEncoder.matches(
                "Password123!",
                savedUser.getPassword()
        ));
    }

    @Test
    void shouldRejectInvalidEmail() throws Exception {

        String json = """
                {
                    "name": "Bob",
                    "email": "invalid-email",
                    "password": "Password123!"
                }
                """;

        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturn404WhenUserNotFound() throws Exception {

        User alice = createTestUser(
                "Alice",
                "alice.notfound.test@example.com"
        );

        mockMvc.perform(get("/api/users/999999999")
                        .with(httpBasic(alice.getEmail(), PASSWORD)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    void shouldReturn409WhenEmailAlreadyExists() throws Exception {

        createTestUser(
                "Existing Alice",
                "duplicate.test@example.com"
        );

        String json = """
                {
                    "name": "Another Alice",
                    "email": "duplicate.test@example.com",
                    "password": "Password123!"
                }
                """;

        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409));
    }

    // =========================
    // User-Project API Tests
    // =========================

    @Test
    void shouldCreateProjectForExistingUser() throws Exception {

        User alice = createTestUser(
                "Alice",
                "alice.project.test@example.com"
        );

        String json = """
                {
                    "name": "Spring Boot Learning"
                }
                """;

        mockMvc.perform(post(
                        "/api/users/{userId}/projects",
                        alice.getId()
                )
                        .with(httpBasic(alice.getEmail(), PASSWORD))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.name")
                        .value("Spring Boot Learning"))
                .andExpect(jsonPath("$.tasks").isArray())
                .andExpect(jsonPath("$.tasks").isEmpty());
    }

    @Test
    void shouldReturn404WhenCreatingProjectForMissingUser()
            throws Exception {

        User alice = createTestUser(
                "Alice",
                "alice.missing.project.test@example.com"
        );

        String json = """
                {
                    "name": "Invalid Project"
                }
                """;

        mockMvc.perform(post("/api/users/999999999/projects")
                        .with(httpBasic(alice.getEmail(), PASSWORD))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldReturnOnlyProjectsBelongingToUser()
            throws Exception {

        User alice = createTestUser(
                "Alice",
                "alice.filter.test@example.com"
        );

        User bob = createTestUser(
                "Bob",
                "bob.filter.test@example.com"
        );

        Project projectA = new Project("Project A");
        projectA.setUser(alice);

        Project projectB = new Project("Project B");
        projectB.setUser(alice);

        Project projectC = new Project("Project C");
        projectC.setUser(bob);

        projectRepository.save(projectA);
        projectRepository.save(projectB);
        projectRepository.save(projectC);

        mockMvc.perform(get(
                        "/api/users/{userId}/projects",
                        alice.getId()
                )
                        .with(httpBasic(alice.getEmail(), PASSWORD)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath(
                        "$[?(@.name == 'Project A')]"
                ).exists())
                .andExpect(jsonPath(
                        "$[?(@.name == 'Project B')]"
                ).exists())
                .andExpect(jsonPath(
                        "$[?(@.name == 'Project C')]"
                ).doesNotExist());
    }

    @Test
    void shouldReturn404WhenGettingProjectsForMissingUser()
            throws Exception {

        User alice = createTestUser(
                "Alice",
                "alice.missing.list.test@example.com"
        );

        mockMvc.perform(get("/api/users/999999999/projects")
                        .with(httpBasic(alice.getEmail(), PASSWORD)))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldRejectProjectWithBlankName() throws Exception {

        User alice = createTestUser(
                "Alice",
                "alice.validation.test@example.com"
        );

        String json = """
                {
                    "name": ""
                }
                """;

        mockMvc.perform(post(
                        "/api/users/{userId}/projects",
                        alice.getId()
                )
                        .with(httpBasic(alice.getEmail(), PASSWORD))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest());
    }

    // =========================
    // Day 11 Ownership Tests
    // =========================

    @Test
    void shouldNotAllowUserToReadAnotherUser()
            throws Exception {

        User alice = createTestUser(
                "Alice",
                "alice.ownership.user.test@example.com"
        );

        User bob = createTestUser(
                "Bob",
                "bob.ownership.user.test@example.com"
        );

        mockMvc.perform(get("/api/users/{id}", bob.getId())
                        .with(httpBasic(alice.getEmail(), PASSWORD)))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldNotAllowUserToReadAnotherUsersProjects()
            throws Exception {

        User alice = createTestUser(
                "Alice",
                "alice.ownership.list.test@example.com"
        );

        User bob = createTestUser(
                "Bob",
                "bob.ownership.list.test@example.com"
        );

        Project bobProject = new Project("Bob's Private Project");
        bobProject.setUser(bob);
        projectRepository.save(bobProject);

        mockMvc.perform(get(
                        "/api/users/{userId}/projects",
                        bob.getId()
                )
                        .with(httpBasic(alice.getEmail(), PASSWORD)))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldNotAllowUserToCreateProjectForAnotherUser()
            throws Exception {

        User alice = createTestUser(
                "Alice",
                "alice.ownership.create.test@example.com"
        );

        User bob = createTestUser(
                "Bob",
                "bob.ownership.create.test@example.com"
        );

        String json = """
                {
                    "name": "Unauthorized Project"
                }
                """;

        mockMvc.perform(post(
                        "/api/users/{userId}/projects",
                        bob.getId()
                )
                        .with(httpBasic(alice.getEmail(), PASSWORD))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isNotFound());

        assertTrue(
                projectRepository.findByUserId(bob.getId()).isEmpty()
        );
    }
}
