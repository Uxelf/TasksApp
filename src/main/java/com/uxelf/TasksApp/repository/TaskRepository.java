package com.uxelf.TasksApp.repository;

import com.uxelf.TasksApp.entity.Task;
import com.uxelf.TasksApp.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TaskRepository extends JpaRepository<Task, Integer> {
    List<Task> findByAuthor(User user);
}
