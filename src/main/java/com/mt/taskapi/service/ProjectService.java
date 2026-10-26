package com.mt.taskapi.service;

import com.mt.taskapi.dto.TaskRequest;
import com.mt.taskapi.exception.ProjectNotFoundException;
import com.mt.taskapi.model.Project;
import com.mt.taskapi.repository.ProjectRepository;
import com.mt.taskapi.repository.TaskRepository;
import com.mt.taskapi.repository.UserRepository;
import org.springframework.stereotype.Service;
import com.mt.taskapi.dto.TaskResponse;
import com.mt.taskapi.model.Task;
import com.mt.taskapi.dto.ProjectResponse;
import java.util.List;
import org.springframework.transaction.annotation.Transactional;
import com.mt.taskapi.dto.ProjectRequest;
import com.mt.taskapi.model.User;
import com.mt.taskapi.exception.UserNotFoundException;
import com.mt.taskapi.security.CurrentUserService;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;



@Service
public class ProjectService {
    private final ProjectRepository projectRepository;
    private final TaskRepository taskRepository;
    private final UserRepository userRepository;
    private final CurrentUserService currentUserService;

    public ProjectService(
            ProjectRepository projectRepository,
            TaskRepository taskRepository,
            UserRepository userRepository,
            CurrentUserService currentUserService) {

        this.projectRepository = projectRepository;
        this.taskRepository = taskRepository;
        this.userRepository = userRepository;
        this.currentUserService = currentUserService;
    }


    @Transactional
    public ProjectResponse createProject(ProjectRequest request) {

        User currentUser = currentUserService.getCurrentUser();

        Project project = new Project(request.name(), currentUser);

        Project saved = projectRepository.save(project);

        return toProjectResponse(saved);
    }


    @Transactional(readOnly = true)
    public List<ProjectResponse> getAllProjects() {

        User currentUser = currentUserService.getCurrentUser();

        return projectRepository.findByUserId(currentUser.getId())
                .stream()
                .map(this::toProjectResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public ProjectResponse getProjectById(Long id) {

        User currentUser = currentUserService.getCurrentUser();

        Project project = projectRepository.findById(id)
                .orElseThrow(() -> new ProjectNotFoundException(id));

        if (project.getUser() == null
                || !project.getUser().getId().equals(currentUser.getId())) {
            throw new ProjectNotFoundException(id);
        }

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

        User currentUser = currentUserService.getCurrentUser();

        if (project.getUser() == null
                || !project.getUser().getId().equals(currentUser.getId())) {
            throw new ProjectNotFoundException(projectId);
        }

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

        User currentUser = currentUserService.getCurrentUser();

        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ProjectNotFoundException(projectId));

        if (project.getUser() == null
                || !project.getUser().getId().equals(currentUser.getId())) {
            throw new ProjectNotFoundException(projectId);
        }

        return taskRepository.findByProjectUserId(projectId)
                .stream()
                .map(this::toTaskResponse)
                .toList();
    }

    @Transactional
    public ProjectResponse createProjectForUser(
            Long userId,
            ProjectRequest request) {

        User currentUser = currentUserService.getCurrentUser();

        if (!currentUser.getId().equals(userId)) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "User projects not found"
            );
        }

        Project project = new Project(request.name(), currentUser);

        Project saved = projectRepository.save(project);

        return toProjectResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<ProjectResponse> getProjectsByUserId(Long userId) {

        User currentUser = currentUserService.getCurrentUser();

        if (!currentUser.getId().equals(userId)) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "User projects not found"
            );
        }

        return projectRepository.findByUserId(userId)
                .stream()
                .map(this::toProjectResponse)
                .toList();
    }

}
