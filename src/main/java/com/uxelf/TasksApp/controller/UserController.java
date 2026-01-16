package com.uxelf.TasksApp.controller;

import com.uxelf.TasksApp.entity.User;
import com.uxelf.TasksApp.repository.UserRepository;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/users")
public class UserController {

    private final UserRepository userRepository;

    public UserController(UserRepository userRepository){
        this.userRepository = userRepository;
    }

    @GetMapping
    public Iterable<User> findAllUsers(){
        return this.userRepository.findAll();
    }

    @PostMapping
    public User addOneUser(@RequestBody @Valid User user){
        return this.userRepository.save(user);
    }
}
