package com.mt.taskapi;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.http.MediaType;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

@SpringBootTest
@AutoConfigureMockMvc
class TaskControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void shouldReturnBadRequestWhenTitleIsBlank() throws Exception {

        String invalidTask = """
            {
              "title": "",
              "description": "test",
              "status": "TODO"
            }
            """;

        mockMvc.perform(
                        post("/api/tasks")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(invalidTask)
                )
                .andExpect(status().isBadRequest());
    }


    @Test
    void shouldReturnNotFoundWhenTaskDoesNotExist() throws Exception {
        mockMvc.perform(
                        get("/api/tasks/999999")
                )
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message")
                        .value("Task with id 999999 not found"));
    }

}