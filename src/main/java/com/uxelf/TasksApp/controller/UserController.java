package com.uxelf.TasksApp.controller;

import com.uxelf.TasksApp.entity.User;
import com.uxelf.TasksApp.repository.UserRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class UserController {

    private final UserRepository userRepository;

    public UserController(UserRepository userRepository){
        this.userRepository = userRepository;
    }

    @GetMapping("/users")
    public Iterable<User> findAllUsers(){
        return this.userRepository.findAll();
    }

    @PostMapping("/users")
    public User addOneUser(@RequestBody User user){
        return this.userRepository.save(user);
    }
}
