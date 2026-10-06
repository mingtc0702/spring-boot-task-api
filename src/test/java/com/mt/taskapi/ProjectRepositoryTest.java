package com.mt.taskapi;

import com.mt.taskapi.model.Project;
import com.mt.taskapi.model.Task;
import com.mt.taskapi.model.TaskStatus;
import com.mt.taskapi.repository.ProjectRepository;
import com.mt.taskapi.repository.TaskRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class ProjectRepositoryTest {

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private TaskRepository taskRepository;


    @Test
    void shouldSaveProject() {
        Project project = new Project("Backend Learning");

        projectRepository.save(project);
    }

    @Test
    void shouldAssignTasksToProject() {
        Project project = projectRepository.findById(1L).orElseThrow();

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

        taskRepository.save(task1);
        taskRepository.save(task2);
    }

}