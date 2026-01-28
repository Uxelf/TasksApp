package com.uxelf.TasksApp.entity;

import com.uxelf.TasksApp.entity.enums.TaskStatus;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "tasks")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Task {
    @Id
    @Setter(AccessLevel.NONE)
    @GeneratedValue
    @Column(columnDefinition = "uuid")
    private UUID id;

    @Setter
    @Column(nullable = false)
    private String title;

    @Setter
    private String description;

    @Enumerated(EnumType.STRING)
    @Setter
    @Column(nullable = false)
    private TaskStatus status = TaskStatus.PENDING;

    @Setter
    @Column(name = "start_time", nullable = false)
    private LocalDate start;

    @Setter
    @Column(name = "end_time", nullable = false)
    private LocalDate end;

    @ManyToOne
    @JoinColumn(name = "author_id")
    private User author;

    // Constructor público original (para uso en producción)
    public Task(String title, String description, LocalDate start, LocalDate end, User authorUser) {
        this.title = title;
        this.description = description;
        this.author = authorUser;
        this.start = start;
        this.end = end;
    }

    // Constructor público para testing - permite setear el ID manualmente
    public Task(UUID id, String title, String description, LocalDate start, LocalDate end, User author) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.start = start;
        this.end = end;
        this.author = author;
        this.status = TaskStatus.PENDING;
    }
}