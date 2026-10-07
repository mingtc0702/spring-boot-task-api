package com.mt.taskapi.service;

import com.mt.taskapi.dto.TaskRequest;
import com.mt.taskapi.exception.ProjectNotFoundException;
import com.mt.taskapi.model.Project;
import com.mt.taskapi.repository.ProjectRepository;
import com.mt.taskapi.repository.TaskRepository;
import org.springframework.stereotype.Service;
import com.mt.taskapi.dto.TaskResponse;
import com.mt.taskapi.model.Task;
import com.mt.taskapi.dto.ProjectResponse;
import java.util.List;
import org.springframework.transaction.annotation.Transactional;



@Service
public class ProjectService {
    private final ProjectRepository projectRepository;
    private final TaskRepository taskRepository;

    public ProjectService(
            ProjectRepository projectRepository,
            TaskRepository taskRepository) {

        this.projectRepository = projectRepository;
        this.taskRepository = taskRepository;
    }

    public Project createProject (Project project){return projectRepository.save(project);}

    @Transactional(readOnly = true)
    public List<ProjectResponse> getAllProjects() {
        return projectRepository.findAll()
                .stream()
                .map(this::toProjectResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public ProjectResponse getProjectById(Long id) {
        Project project = projectRepository.findById(id)
                .orElseThrow(() -> new ProjectNotFoundException(id));

        return toProjectResponse(project);
    }

    private TaskResponse toTaskResponse(Task task) {
        return new TaskResponse(
                task.getId(),
                task.getTitle(),
                task.getDescription(),
                task.getStatus()
        );
    }

    private ProjectResponse toProjectResponse(Project project) {

        List<TaskResponse> taskResponses = project.getTasks()
                .stream()
                .map(this::toTaskResponse)
                .toList();

        return new ProjectResponse(
                project.getId(),
                project.getName(),
                taskResponses
        );
    }

    @Transactional
    public TaskResponse addTaskToProject(Long projectId, TaskRequest request) {

        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ProjectNotFoundException(projectId));

        Task task = new Task(
                request.title(),
                request.description(),
                request.status(),
                project
        );

        Task savedTask = taskRepository.save(task);

        return toTaskResponse(savedTask);
    }

    @Transactional(readOnly = true)
    public List<TaskResponse> getTasksByProjectId(Long projectId) {

        if (!projectRepository.existsById(projectId)) {
            throw new ProjectNotFoundException(projectId);
        }

        return taskRepository.findByProjectId(projectId)
                .stream()
                .map(this::toTaskResponse)
                .toList();
    }




}
