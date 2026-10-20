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
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@WithMockUser
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

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

        mockMvc.perform(get("/api/users/999999999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    void shouldReturn409WhenEmailAlreadyExists() throws Exception {

        User existingUser = new User(
                "Existing Alice",
                "duplicate.test@example.com",
                passwordEncoder.encode("ExistingPassword123!")
        );

        userRepository.save(existingUser);

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

        User user = userRepository.save(
                new User(
                        "Alice",
                        "alice.project.test@example.com",
                        passwordEncoder.encode("Password123!")
                )
        );

        String json = """
                {
                    "name": "Spring Boot Learning"
                }
                """;

        mockMvc.perform(post("/api/users/{userId}/projects", user.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.name").value("Spring Boot Learning"))
                .andExpect(jsonPath("$.tasks").isArray())
                .andExpect(jsonPath("$.tasks").isEmpty());
    }

    @Test
    void shouldReturn404WhenCreatingProjectForMissingUser() throws Exception {

        String json = """
                {
                    "name": "Invalid Project"
                }
                """;

        mockMvc.perform(post("/api/users/999999999/projects")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    void shouldReturnOnlyProjectsBelongingToUser() throws Exception {

        User alice = userRepository.save(
                new User(
                        "Alice",
                        "alice.filter.test@example.com",
                        passwordEncoder.encode("Password123!")
                )
        );

        User bob = userRepository.save(
                new User(
                        "Bob",
                        "bob.filter.test@example.com",
                        passwordEncoder.encode("Password123!")
                )
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

        mockMvc.perform(get("/api/users/{userId}/projects", alice.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[?(@.name == 'Project A')]").exists())
                .andExpect(jsonPath("$[?(@.name == 'Project B')]").exists())
                .andExpect(jsonPath("$[?(@.name == 'Project C')]").doesNotExist());
    }

    @Test
    void shouldReturn404WhenGettingProjectsForMissingUser() throws Exception {

        mockMvc.perform(get("/api/users/999999999/projects"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    void shouldRejectProjectWithBlankName() throws Exception {

        User user = userRepository.save(
                new User(
                        "Alice",
                        "alice.validation.test@example.com",
                        passwordEncoder.encode("Password123!")
                )
        );

        String json = """
                {
                    "name": ""
                }
                """;

        mockMvc.perform(post("/api/users/{userId}/projects", user.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest());
    }
}