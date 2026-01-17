package com.uxelf.TasksApp.repository;

import com.uxelf.TasksApp.entity.Task;
import com.uxelf.TasksApp.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface TaskRepository extends JpaRepository<Task, UUID> {
    List<Task> findByAuthor(User user);
    List<Task> findByAuthorId(UUID userId);

    @Query("""
        SELECT t
        FROM Task t
        WHERE t.author.id = :userId
          AND t.start <= :date
          AND t.end >= :date
    """)
    List<Task> findTasksOverlappingDay(
            @Param("userId") UUID userId,
            @Param("date") LocalDate date
    );

    @Query("""
        SELECT t
        FROM Task t
        WHERE t.author.id = :userId
          AND t.start <= :monthEnd
          AND t.end >= :monthStart
    """)
    List<Task> findTasksOverlappingMonth(
            @Param("userId") UUID userId,
            @Param("monthStart") LocalDate monthStart,
            @Param("monthEnd") LocalDate monthEnd
    );
}
