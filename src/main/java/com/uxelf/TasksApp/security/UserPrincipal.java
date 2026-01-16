package com.uxelf.TasksApp.security;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.UUID;

@AllArgsConstructor
@Getter
public class UserPrincipal {
    private final UUID id;
    private final String username;
}
