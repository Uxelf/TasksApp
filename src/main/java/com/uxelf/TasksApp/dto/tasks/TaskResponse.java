package com.uxelf.TasksApp.dto.tasks;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@AllArgsConstructor
@Getter
public class TaskResponse {
    private Integer id;
    private String title;
    private String description;
    private LocalDateTime start;
    private LocalDateTime end;
}
