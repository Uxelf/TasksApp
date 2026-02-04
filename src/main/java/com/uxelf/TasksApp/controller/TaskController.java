package com.uxelf.TasksApp.controller;

import com.uxelf.TasksApp.dto.tasks.CreateTaskRequest;
import com.uxelf.TasksApp.dto.tasks.TaskResponse;
import com.uxelf.TasksApp.dto.tasks.UpdateTaskRequest;
import com.uxelf.TasksApp.entity.Task;
import com.uxelf.TasksApp.entity.enums.TaskStatus;
import com.uxelf.TasksApp.repository.TaskRepository;
import com.uxelf.TasksApp.security.UserPrincipal;
import com.uxelf.TasksApp.service.TaskService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.UUID;

@AllArgsConstructor
@RestController
@RequestMapping("/tasks")
public class TaskController {

    private final TaskRepository taskRepository;
    private final TaskService taskService;


    @GetMapping
    public ResponseEntity<List<TaskResponse>> getUserTasks(@AuthenticationPrincipal UserPrincipal user){
        List<TaskResponse> tasksResponses = taskService.getTasksByUser(user.getId());
        return ResponseEntity.ok(tasksResponses);
    }

    @PostMapping
    public ResponseEntity<TaskResponse> createTask(
            @RequestBody @Valid CreateTaskRequest taskRequest,
            @AuthenticationPrincipal UserPrincipal user
    ){
        TaskResponse taskResponse = taskService.createTask(taskRequest, user.getId());
        return ResponseEntity.ok(taskResponse);
    }

    @GetMapping("/day")
    public ResponseEntity<List<TaskResponse>> getDayTasks(
            @RequestParam LocalDate date,
            @AuthenticationPrincipal UserPrincipal user
            ){
        List<TaskResponse> taskResponses = taskService.getTaskForDay(user.getId(), date);
        return ResponseEntity.ok(taskResponses);
    }

    @GetMapping("/month")
    public ResponseEntity<List<TaskResponse>> getMonthTasks(
            @RequestParam YearMonth date,
            @AuthenticationPrincipal UserPrincipal user
    ){
        List<TaskResponse> taskResponses = taskService.getTaskForMonth(user.getId(), date);
        return ResponseEntity.ok(taskResponses);
    }

    @GetMapping("/{id}")
    public ResponseEntity<TaskResponse> getTask(
            @PathVariable UUID id,
            @AuthenticationPrincipal UserPrincipal user
    ){
        TaskResponse taskResponse = taskService.getTaskById(id, user.getId());
        return ResponseEntity.ok(taskResponse);
    }

    @PutMapping("/{id}")
    public ResponseEntity<TaskResponse> updateTask(
            @PathVariable UUID id,
            @RequestBody UpdateTaskRequest taskRequest,
            @AuthenticationPrincipal UserPrincipal user
    ){
        TaskResponse taskResponse = taskService.updateTask(id, taskRequest, user.getId());
        return ResponseEntity.ok(taskResponse);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTask(@PathVariable UUID id, @AuthenticationPrincipal UserPrincipal user){
        taskService.deleteTask(id, user.getId());
        return ResponseEntity.ok().build();
    }

    @GetMapping("/status")
    public ResponseEntity<TaskStatus[]> getStatuses(){
        return ResponseEntity.ok(TaskStatus.values());
    }
}
