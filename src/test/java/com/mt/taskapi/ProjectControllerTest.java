package com.mt.taskapi;

import com.mt.taskapi.model.Project;
import com.mt.taskapi.model.Task;
import com.mt.taskapi.model.TaskStatus;
import com.mt.taskapi.repository.ProjectRepository;
import com.mt.taskapi.repository.TaskRepository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class ProjectControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private TaskRepository taskRepository;

    @Test
    void shouldReturn404WhenProjectDoesNotExist() throws Exception {
        mockMvc.perform(get("/api/projects/999999"))
                .andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    void shouldCreateTaskForProject() throws Exception {

        Project project = projectRepository.save(
                new Project("MockMvc Test Project")
        );

        mockMvc.perform(post("/api/projects/" + project.getId() + "/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {
                                "title": "Test MockMvc",
                                "description": "Testing POST endpoint",
                                "status": "TODO"
                            }
                            """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title").value("Test MockMvc"))
                .andExpect(jsonPath("$.status").value("TODO"))
                .andExpect(jsonPath("$.id").isNumber());
    }

    @Test
    @Transactional
    void shouldRejectTaskWithBlankTitle() throws Exception {

        Project project = projectRepository.save(
                new Project("Validation Test Project")
        );

        mockMvc.perform(post("/api/projects/" + project.getId() + "/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {
                                "title": "",
                                "description": "Invalid task",
                                "status": "TODO"
                            }
                            """))
                .andExpect(status().isBadRequest());
    }

    // New Test 1: TaskResponse should not expose Project Entity
    @Test
    @Transactional
    void shouldReturnTaskResponseWithoutProject() throws Exception {

        Project project = projectRepository.save(
                new Project("DTO Test Project")
        );

        Task task = taskRepository.save(
                new Task(
                        "Test DTO Response",
                        "Verify response fields",
                        TaskStatus.TODO,
                        project
                )
        );

        mockMvc.perform(get("/api/tasks/" + task.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(task.getId()))
                .andExpect(jsonPath("$.title").value("Test DTO Response"))
                .andExpect(jsonPath("$.status").value("TODO"))
                .andExpect(jsonPath("$.project").doesNotExist());
    }

    // New Test 2: ProjectRequest validation
    @Test
    @Transactional
    void shouldRejectProjectWithBlankName() throws Exception {

        mockMvc.perform(post("/api/projects")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {
                                "name": ""
                            }
                            """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("name: must not be blank"));
    }
}
