package com.mt.taskapi.controller;

import com.mt.taskapi.dto.UserRequest;
import com.mt.taskapi.dto.UserResponse;
import com.mt.taskapi.service.ProjectService;
import com.mt.taskapi.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.mt.taskapi.dto.ProjectRequest;
import com.mt.taskapi.dto.ProjectResponse;
import com.mt.taskapi.service.ProjectService;

import java.util.List;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;
    private final ProjectService projectService;

    public UserController(
            UserService userService,
            ProjectService projectService) {

        this.userService = userService;
        this.projectService = projectService;
    }

    @PostMapping
    public ResponseEntity<UserResponse> createUser(
            @Valid @RequestBody UserRequest request) {

        UserResponse response = userService.createUser(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserResponse> getUserById(
            @PathVariable Long id) {

        UserResponse response = userService.getUserById(id);

        return ResponseEntity.ok(response);
    }


    @PostMapping("/{userId}/projects")
    public ResponseEntity<ProjectResponse> createProjectForUser(
            @PathVariable Long userId,
            @Valid @RequestBody ProjectRequest request) {

        ProjectResponse response =
                projectService.createProjectForUser(userId, request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    @GetMapping("/{userId}/projects")
    public ResponseEntity<List<ProjectResponse>> getProjectsByUserId(
            @PathVariable Long userId) {

        List<ProjectResponse> responses = projectService.getProjectsByUserId(userId);

        return ResponseEntity.ok(responses);
    }
}
