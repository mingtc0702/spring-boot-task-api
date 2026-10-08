package com.mt.taskapi.service;

import com.mt.taskapi.dto.TaskRequest;
import com.mt.taskapi.dto.TaskResponse;
import com.mt.taskapi.exception.TaskNotFoundException;
import com.mt.taskapi.model.Task;
import com.mt.taskapi.repository.TaskRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TaskService {

    private final TaskRepository taskRepository;

    public TaskService(TaskRepository taskRepository) {
        this.taskRepository = taskRepository;
    }

    public List<TaskResponse> getAllTasks() {
        return taskRepository.findAll()
                .stream()
                .map(this::toTaskResponse)
                .toList();
    }

    public TaskResponse getTaskById(Long id) {
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new TaskNotFoundException(id));

        return toTaskResponse(task);
    }

    public TaskResponse createTask(TaskRequest request) {

        Task task = new Task(
                request.title(),
                request.description(),
                request.status(),
                null
        );

        Task savedTask = taskRepository.save(task);

        return toTaskResponse(savedTask);
    }

    public TaskResponse updateTask(Long id, TaskRequest request) {

        Task existingTask = taskRepository.findById(id)
                .orElseThrow(() -> new TaskNotFoundException(id));

        existingTask.setTitle(request.title());
        existingTask.setDescription(request.description());
        existingTask.setStatus(request.status());

        Task savedTask = taskRepository.save(existingTask);

        return toTaskResponse(savedTask);
    }

    public void deleteTask(Long id) {

        Task existingTask = taskRepository.findById(id)
                .orElseThrow(() -> new TaskNotFoundException(id));

        taskRepository.delete(existingTask);
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