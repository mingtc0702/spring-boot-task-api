package com.mt.taskapi.controller;

import com.mt.taskapi.dto.ProjectResponse;
import com.mt.taskapi.dto.TaskRequest;
import com.mt.taskapi.dto.TaskResponse;
import com.mt.taskapi.model.Project;
import com.mt.taskapi.model.Task;
import com.mt.taskapi.service.ProjectService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/projects")
public class ProjectController {
    private final ProjectService projectService;

    public ProjectController(ProjectService projectService){this.projectService = projectService;}

    @PostMapping
    public Project createProject(@Valid @RequestBody Project project){return projectService.createProject(project);}

    @GetMapping
    public List<ProjectResponse> getAllProjects() {
        return projectService.getAllProjects();
    }

    @GetMapping("/{id}")
    public ProjectResponse getProjectById(@PathVariable Long id) {
        return projectService.getProjectById(id);
    }

    @PostMapping("/{projectId}/tasks")
    @ResponseStatus(HttpStatus.CREATED)
    public TaskResponse addTaskToProject(
            @PathVariable Long projectId,
            @Valid @RequestBody TaskRequest request) {

        return projectService.addTaskToProject(projectId, request);
    }

    @GetMapping("/{projectId}/tasks")
    public List<TaskResponse> getTasksByProjectId(
            @PathVariable Long projectId) {

        return projectService.getTasksByProjectId(projectId);
    }


}
