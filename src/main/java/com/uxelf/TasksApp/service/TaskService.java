package com.uxelf.TasksApp.service;

import com.uxelf.TasksApp.dto.tasks.CreateTaskRequest;
import com.uxelf.TasksApp.dto.tasks.TaskResponse;
import com.uxelf.TasksApp.dto.tasks.UpdateTaskRequest;
import com.uxelf.TasksApp.entity.Task;
import com.uxelf.TasksApp.entity.User;
import com.uxelf.TasksApp.exception.BusinessException;
import com.uxelf.TasksApp.repository.TaskRepository;
import com.uxelf.TasksApp.repository.UserRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
@AllArgsConstructor
public class TaskService {

    private final TaskRepository taskRepository;
    private final UserRepository userRepository;

    public TaskResponse createTask(CreateTaskRequest request, UUID userId){
        if (request.getEnd().isBefore(request.getStart())) {
            throw new IllegalArgumentException("End date must be after start date");
        }

        if (request.getEnd().isBefore(LocalDate.now())) {
            throw new IllegalArgumentException("End date must be in the future");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException("User not found"));

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

    public TaskResponse getTaskById(UUID taskId, UUID userId){
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new BusinessException("Task not found"));

        if (!task.getAuthor().getId().equals(userId)){
            throw new BusinessException("You don't have permission to see this task");
        }

        return mapToResponse(task);
    }

    public List<TaskResponse> getTasksByUser(UUID userId){

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException("User not found"));

        List<Task> tasks = taskRepository.findByAuthor(user);
        System.out.println(tasks);
        return taskRepository.findByAuthor(user)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    public TaskResponse updateTask(UUID taskId, UpdateTaskRequest request, UUID userId){

        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new BusinessException("Task not found"));

        if (!task.getAuthor().getId().equals(userId)){
            throw new BusinessException("You don't have permission to modify this task");
        }

        if (request.getTitle() != null){
            if (request.getTitle().trim().isBlank())
                throw new BusinessException("Title can't be empty");
            task.setTitle(request.getTitle());
        }

        if (request.getDescription() != null)
            task.setDescription(request.getDescription());

        if (request.getStatus() != null)
            task.setStatus(request.getStatus());

        if (request.getStart() != null)
            task.setStart(request.getStart());

        if (request.getEnd() != null)
            task.setEnd(request.getEnd());

        if (task.getEnd().isBefore(task.getStart()))
            throw new BusinessException("End date must be after start date");


        Task updated = taskRepository.save(task);
        return mapToResponse(updated);
    }

    public void deleteTask(UUID taskId, UUID userId){

        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new BusinessException("Task not found"));

        if (!task.getAuthor().getId().equals(userId)){
            throw new BusinessException("You don't have permission to modify this task");
        }

        taskRepository.delete(task);
    }

    private TaskResponse mapToResponse(Task task) {
        return new TaskResponse(
                task.getId(),
                task.getTitle(),
                task.getDescription(),
                task.getStatus(),
                task.getStart(),
                task.getEnd(),
                task.getEnd().isBefore(LocalDate.now())
        );
    }
}
