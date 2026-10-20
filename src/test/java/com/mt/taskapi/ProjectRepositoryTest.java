package com.mt.taskapi;

import com.mt.taskapi.model.Project;
import com.mt.taskapi.model.Task;
import com.mt.taskapi.model.TaskStatus;
import com.mt.taskapi.repository.ProjectRepository;
import com.mt.taskapi.repository.TaskRepository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class ProjectRepositoryTest {

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private TaskRepository taskRepository;

    @Test
    void shouldSaveProject() {

        Project project = new Project("Backend Learning");

        Project savedProject = projectRepository.save(project);

        assertNotNull(savedProject.getId());
        assertEquals("Backend Learning", savedProject.getName());
    }

    @Test
    void shouldAssignTasksToProject() {

        Project project = projectRepository.save(
                new Project("Task Assignment Test")
        );

        Task task1 = new Task(
                "Learn JPA",
                "Learn JPA relationships",
                TaskStatus.DONE,
                project
        );

        Task task2 = new Task(
                "Learn PostgreSQL",
                "Practice foreign keys and joins",
                TaskStatus.DONE,
                project
        );

        Task savedTask1 = taskRepository.save(task1);
        Task savedTask2 = taskRepository.save(task2);

        assertNotNull(savedTask1.getId());
        assertNotNull(savedTask2.getId());

        assertEquals(project.getId(), savedTask1.getProject().getId());
        assertEquals(project.getId(), savedTask2.getProject().getId());
    }
}