package com.uxelf.TasksApp.dto.tasks;

import com.uxelf.TasksApp.entity.enums.TaskStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDate;
import java.util.UUID;

@AllArgsConstructor
@Getter
public class TaskResponse {
    private UUID id;
    private String title;
    private String description;
    private TaskStatus status;
    private LocalDate start;
    private LocalDate end;
    private boolean expired;
}
