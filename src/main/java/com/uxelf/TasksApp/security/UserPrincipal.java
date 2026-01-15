package com.uxelf.TasksApp.security;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class UserPrincipal {
    private final Integer id;
    private final String username;
}
