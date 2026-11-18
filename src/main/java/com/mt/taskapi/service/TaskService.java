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
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

@Service
public class TaskService {

    private final TaskRepository taskRepository;
    private final CurrentUserService currentUserService;
    private final CacheManager cacheManager;

    public TaskService(TaskRepository taskRepository, CurrentUserService currentUserService, CacheManager cacheManager) {
        this.taskRepository = taskRepository;
        this.currentUserService = currentUserService;
        this.cacheManager = cacheManager;
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



    @Transactional
    public TaskResponse updateTask(Long id, TaskRequest request) {

        Task existingTask = findOwnedTask(id);

        Long projectId = existingTask.getProject().getId();

        existingTask.setTitle(request.title());
        existingTask.setDescription(request.description());
        existingTask.setStatus(request.status());

        Task savedTask = taskRepository.save(existingTask);

        evictProjectAfterCommit(projectId);

        return toTaskResponse(savedTask);
    }


    @Transactional
    public void deleteTask(Long id) {

        Task existingTask = findOwnedTask(id);

        Long projectId = existingTask.getProject().getId();

        taskRepository.delete(existingTask);

        evictProjectAfterCommit(projectId);
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


    private void evictProjectAfterCommit(Long projectId) {

        if (!TransactionSynchronizationManager
                .isActualTransactionActive()
                || !TransactionSynchronizationManager
                .isSynchronizationActive()) {
            throw new IllegalStateException(
                    "Project cache eviction requires an active transaction"
            );
        }

        TransactionSynchronizationManager.registerSynchronization(
                new TransactionSynchronization() {
                    @Override
                    public void afterCommit() {
                        Cache cache = cacheManager.getCache("projects");

                        if (cache != null) {
                            cache.evictIfPresent(projectId);
                        }
                    }
                }
        );
    }

}