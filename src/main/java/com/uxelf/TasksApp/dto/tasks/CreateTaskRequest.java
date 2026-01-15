package com.uxelf.TasksApp.dto.tasks;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class CreateTaskRequest {
    @NotEmpty(message = "Title is required")
    private String title;
    @NotEmpty(message = "Description is required")
    private String description;
    @NotNull(message = "Start date is required")
    private LocalDateTime start;
    @NotNull(message = "End date is required")
    private LocalDateTime end;
}
