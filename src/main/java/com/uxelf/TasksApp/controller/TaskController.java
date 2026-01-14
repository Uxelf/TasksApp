package com.uxelf.TasksApp.controller;

import com.uxelf.TasksApp.entity.Task;
import com.uxelf.TasksApp.repository.TaskRepository;
import org.springframework.web.bind.annotation.*;

@RestController
public class TaskController {

    private final TaskRepository taskRepository;

    public TaskController(TaskRepository taskRepository){
        this.taskRepository = taskRepository;
    }

    @GetMapping("/tasks")
    public Iterable<Task> findAllTasks(){
        return this.taskRepository.findAll();
    }

    @PostMapping("/tasks")
    public Task addOneTask(@RequestBody Task task){
        return this.taskRepository.save(task);
    }
}
