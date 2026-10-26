package com.mt.taskapi.service;

import com.mt.taskapi.dto.TaskRequest;
import com.mt.taskapi.dto.TaskResponse;
import com.mt.taskapi.exception.TaskNotFoundException;
import com.mt.taskapi.model.Task;
import com.mt.taskapi.model.User;
import com.mt.taskapi.repository.TaskRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import com.mt.taskapi.security.CurrentUserService;

@Service
public class TaskService {

    private final TaskRepository taskRepository;
    private final CurrentUserService currentUserService;

    public TaskService(TaskRepository taskRepository, CurrentUserService currentUserService) {
        this.taskRepository = taskRepository;
        this.currentUserService = currentUserService;
    }

    public List<TaskResponse> getAllTasks() {

        User currentUser = currentUserService.getCurrentUser();

        return taskRepository.findByProjectUserId(currentUser.getId())
                .stream()
                .map(this::toTaskResponse)
                .toList();
    }

    public TaskResponse getTaskById(Long id) {
        Task task = findOwnedTask(id);
        return toTaskResponse(task);
    }


    public TaskResponse updateTask(Long id, TaskRequest request) {

        Task existingTask = findOwnedTask(id);

        existingTask.setTitle(request.title());
        existingTask.setDescription(request.description());
        existingTask.setStatus(request.status());

        Task savedTask = taskRepository.save(existingTask);

        return toTaskResponse(savedTask);
    }

    public void deleteTask(Long id) {
        Task existingTask = findOwnedTask(id);
        taskRepository.delete(existingTask);
    }

    private Task findOwnedTask(Long taskId) {

        User currentUser = currentUserService.getCurrentUser();

        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new TaskNotFoundException(taskId));

        if (task.getProject() == null
                || task.getProject().getUser() == null
                || !task.getProject().getUser().getId()
                .equals(currentUser.getId())) {

            throw new TaskNotFoundException(taskId);
        }

        return task;
    }

    private TaskResponse toTaskResponse(Task task) {
        return new TaskResponse(
                task.getId(),
                task.getTitle(),
                task.getDescription(),
                task.getStatus()
        );
    }
}