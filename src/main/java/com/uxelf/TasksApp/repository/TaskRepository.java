package com.uxelf.TasksApp.repository;

import com.uxelf.TasksApp.entity.Task;
import com.uxelf.TasksApp.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface TaskRepository extends JpaRepository<Task, UUID> {
    List<Task> findByAuthor(User user);
}
