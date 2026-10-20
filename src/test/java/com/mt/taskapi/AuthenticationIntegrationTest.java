package com.mt.taskapi;

import com.mt.taskapi.model.User;
import com.mt.taskapi.repository.UserRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class AuthenticationIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    void setUp() {
        User user = new User(
                "Auth Test User",
                "auth.test@example.com",
                passwordEncoder.encode("Password123!")
        );

        userRepository.saveAndFlush(user);
    }

    @Test
    void shouldAuthenticateWithCorrectPassword() throws Exception {
        mockMvc.perform(get("/api/users/999999999")
                        .with(httpBasic(
                                "auth.test@example.com",
                                "Password123!"
                        )))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldRejectIncorrectPassword() throws Exception {
        mockMvc.perform(get("/api/users/999999999")
                        .with(httpBasic(
                                "auth.test@example.com",
                                "WrongPassword"
                        )))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldRejectUnknownEmail() throws Exception {
        mockMvc.perform(get("/api/users/999999999")
                        .with(httpBasic(
                                "unknown@example.com",
                                "Password123!"
                        )))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldRejectUnauthenticatedRequest() throws Exception {
        mockMvc.perform(get("/api/users/999999999"))
                .andExpect(status().isUnauthorized());
    }
}