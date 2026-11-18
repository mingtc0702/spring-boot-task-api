package com.mt.taskapi.service;

import com.mt.taskapi.dto.ProjectResponse;
import com.mt.taskapi.dto.TaskResponse;
import com.mt.taskapi.exception.ProjectNotFoundException;
import com.mt.taskapi.model.Project;
import com.mt.taskapi.model.Task;
import com.mt.taskapi.repository.ProjectRepository;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ProjectCacheService {

    private final ProjectRepository projectRepository;

    public ProjectCacheService(ProjectRepository projectRepository) {
        this.projectRepository = projectRepository;
    }

    @Cacheable(value = "projects", key = "#projectId")
    @Transactional(readOnly = true)
    public ProjectResponse getCachedProject(Long projectId) {

        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ProjectNotFoundException(projectId));

        List<TaskResponse> tasks = project.getTasks()
                .stream()
                .map(task -> new TaskResponse(
                        task.getId(),
                        task.getTitle(),
                        task.getDescription(),
                        task.getStatus()
                ))
                .toList();

        return new ProjectResponse(
                project.getId(),
                project.getName(),
                tasks
        );
    }
}
