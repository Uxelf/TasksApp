package com.uxelf.TasksApp.service;

import com.uxelf.TasksApp.dto.tasks.CreateTaskRequest;
import com.uxelf.TasksApp.dto.tasks.TaskResponse;
import com.uxelf.TasksApp.dto.tasks.UpdateTaskRequest;
import com.uxelf.TasksApp.entity.Task;
import com.uxelf.TasksApp.entity.User;
import com.uxelf.TasksApp.entity.enums.TaskStatus;
import com.uxelf.TasksApp.exception.BusinessException;
import com.uxelf.TasksApp.repository.TaskRepository;
import com.uxelf.TasksApp.repository.UserRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.UUID;

@Service
@AllArgsConstructor
public class TaskService {

    private static final int MAX_TITLE_LENGTH = 255;
    private static final int MAX_DESCRIPTION_LENGTH = 5000;
    private static final int MAX_YEARS_IN_FUTURE = 10;

    private final TaskRepository taskRepository;
    private final UserRepository userRepository;

    public TaskResponse createTask(CreateTaskRequest request, UUID userId){
        validateTitle(request.getTitle());
        validateDescription(request.getDescription());
        validateDates(request.getStart(), request.getEnd());

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException("User not found"));

        Task task = new Task(
                request.getTitle().trim(),
                request.getDescription() != null ? request.getDescription().trim() : null,
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
        return taskRepository.findByAuthorId(userId)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    public List<TaskResponse> getTaskForDay(UUID userId, LocalDate date){
        List<Task> tasks = taskRepository.findTasksOverlappingDay(userId, date);
        return tasks.stream()
                .map(this::mapToResponse)
                .toList();
    }

    public List<TaskResponse> getTaskForMonth(UUID userId, YearMonth date){
        LocalDate monthStart = date.atDay(1);
        LocalDate monthEnd = date.atEndOfMonth();

        List<Task> tasks = taskRepository.findTasksOverlappingMonth(userId, monthStart, monthEnd);
        return tasks.stream()
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
            validateTitle(request.getTitle());
            task.setTitle(request.getTitle().trim());
        }

        if (request.getDescription() != null){
            validateDescription(request.getDescription());
            task.setDescription(request.getDescription().trim());
        }

        if (request.getStatus() != null){
            task.setStatus(request.getStatus());
        }

        if (request.getStart() != null){
            task.setStart(request.getStart());
        }

        if (request.getEnd() != null){
            task.setEnd(request.getEnd());
        }

        if (task.getEnd().isBefore(task.getStart())){
            throw new BusinessException("End date must be after start date");
        }

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

    private void validateTitle(String title) {
        if (title == null || title.trim().isEmpty()) {
            throw new IllegalArgumentException("Title can't be empty or whitespace");
        }

        if (title.length() > MAX_TITLE_LENGTH) {
            throw new IllegalArgumentException("Title cannot exceed " + MAX_TITLE_LENGTH + " characters");
        }
    }

    private void validateDescription(String description) {
        if (description != null && description.length() > MAX_DESCRIPTION_LENGTH) {
            throw new IllegalArgumentException("Description cannot exceed " + MAX_DESCRIPTION_LENGTH + " characters");
        }
    }

    private void validateDates(LocalDate start, LocalDate end) {
        if (start == null) {
            throw new IllegalArgumentException("Start date cannot be null");
        }

        if (end == null) {
            throw new IllegalArgumentException("End date cannot be null");
        }

        if (end.isBefore(start)) {
            throw new IllegalArgumentException("End date must be after start date");
        }

        if (end.isBefore(LocalDate.now())) {
            throw new IllegalArgumentException("End date must be in the future");
        }

        if (end.isAfter(LocalDate.now().plusYears(MAX_YEARS_IN_FUTURE))) {
            throw new IllegalArgumentException("End date cannot be more than " + MAX_YEARS_IN_FUTURE + " years in the future");
        }
    }

    private TaskResponse mapToResponse(Task task) {
        return new TaskResponse(
                task.getId(),
                task.getTitle(),
                task.getDescription(),
                task.getStatus(),
                task.getStart(),
                task.getEnd(),
                task.getStatus() != TaskStatus.COMPLETED && task.getEnd().isBefore(LocalDate.now())
        );
    }
}
