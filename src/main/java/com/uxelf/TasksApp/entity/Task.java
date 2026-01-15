package com.uxelf.TasksApp.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.antlr.v4.runtime.misc.NotNull;

import java.time.LocalDateTime;

@Entity
@Table(name = "tasks")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Task {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Setter(AccessLevel.NONE)
    private Integer id;

    @Setter
    @Column(nullable = false)
    private String title;
    @Setter
    private String description;

    @Setter
    @Column(name = "start_time", nullable = false)
    private LocalDateTime start;
    @Setter
    @Column(name = "end_time", nullable = false)
    private LocalDateTime end;

    @ManyToOne
    @JoinColumn(name = "author_id")
    private User author;

    public Task(String title, String description, LocalDateTime start, LocalDateTime end, User authorUser) {
        this.title = title;
        this.description = description;
        this.author = authorUser;
        this.start = start;
        this.end = end;
    }
}
