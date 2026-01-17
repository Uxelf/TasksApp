package com.uxelf.TasksApp.service;

import com.uxelf.TasksApp.entity.User;
import com.uxelf.TasksApp.exception.BusinessException;
import com.uxelf.TasksApp.repository.UserRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@AllArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    public User getUserById(UUID id){
        return userRepository.findById(id)
                .orElseThrow(() -> new BusinessException("User not found"));
    }
}
