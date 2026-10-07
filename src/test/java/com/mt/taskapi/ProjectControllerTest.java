package com.mt.taskapi;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


import org.springframework.transaction.annotation.Transactional;
import org.springframework.http.MediaType;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

import com.mt.taskapi.model.Project;
import com.mt.taskapi.repository.ProjectRepository;

@SpringBootTest
@AutoConfigureMockMvc
class ProjectControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ProjectRepository projectRepository;

    @Test
    void shouldReturn404WhenProjectDoesNotExist() throws Exception {
        mockMvc.perform(get("/api/projects/999999"))
                .andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    void shouldCreateTaskForProject() throws Exception {

        // 1. Arrange: 创建测试需要的 Project
        Project project = projectRepository.save(
                new Project("MockMvc Test Project")
        );

        // 2. Act + Assert: 发送请求并验证结果
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
}

