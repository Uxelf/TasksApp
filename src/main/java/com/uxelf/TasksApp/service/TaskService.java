package com.uxelf.TasksApp.service;

import com.uxelf.TasksApp.dto.tasks.CreateTaskRequest;
import com.uxelf.TasksApp.dto.tasks.TaskResponse;
import com.uxelf.TasksApp.dto.tasks.UpdateTaskRequest;
import com.uxelf.TasksApp.entity.Task;
import com.uxelf.TasksApp.entity.User;
import com.uxelf.TasksApp.repository.TaskRepository;
import com.uxelf.TasksApp.repository.UserRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@AllArgsConstructor
public class TaskService {

    private final TaskRepository taskRepository;
    private final UserRepository userRepository;

    public TaskResponse createTask(CreateTaskRequest request, Integer userId){
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Task task = new Task(
                request.getTitle(),
                request.getDescription(),
                request.getStart(),
                request.getEnd(),
                user
        );

        Task saved = taskRepository.save(task);

        return mapToResponse(saved);
    }

    public TaskResponse getTaskById(Integer taskId, Integer userId){
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new RuntimeException("Task not found"));

        if (!task.getAuthor().getId().equals(userId)){
            throw new RuntimeException("You don't have permission to see this task");
        }

        return mapToResponse(task);
    }

    public List<TaskResponse> getTasksByUser(Integer userId){

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        List<Task> tasks = taskRepository.findByAuthor(user);
        System.out.println(tasks);
        return taskRepository.findByAuthor(user)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    public TaskResponse updateTask(Integer taskId, UpdateTaskRequest request, Integer userId){
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new RuntimeException("Task not found"));

        if (!task.getAuthor().getId().equals(userId)){
            throw new RuntimeException("You don't have permission to modify this task");
        }

        task.setTitle(request.getTitle());
        task.setDescription(request.getDescription());
        task.setStart(request.getStart());
        task.setEnd(request.getEnd());

        Task updated = taskRepository.save(task);
        return mapToResponse(updated);
    }

    public void deleteTask(Integer taskId, Integer userId){

        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new RuntimeException("Task not found"));

        if (!task.getAuthor().getId().equals(userId)){
            throw new RuntimeException("You don't have permission to modify this task");
        }

        taskRepository.delete(task);
    }

    private TaskResponse mapToResponse(Task task) {
        return new TaskResponse(
                task.getId(),
                task.getTitle(),
                task.getDescription(),
                task.getStart(),
                task.getEnd()
        );
    }
}
