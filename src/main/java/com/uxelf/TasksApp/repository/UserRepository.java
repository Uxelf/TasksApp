package com.uxelf.TasksApp.repository;

import com.uxelf.TasksApp.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Integer> {
}
