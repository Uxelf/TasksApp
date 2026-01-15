package com.uxelf.TasksApp.dto.tasks;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class UpdateTaskRequest {
    private String title;
    private String description;
    private LocalDateTime start;
    private LocalDateTime end;
}
