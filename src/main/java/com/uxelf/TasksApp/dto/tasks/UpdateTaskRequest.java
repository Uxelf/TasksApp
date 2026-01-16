package com.uxelf.TasksApp.dto.tasks;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.uxelf.TasksApp.entity.enums.TaskStatus;
import lombok.Data;

import java.time.LocalDate;

@Data
public class UpdateTaskRequest {

    private String title;
    private String description;
    private TaskStatus status;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate start;
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate end;
}
