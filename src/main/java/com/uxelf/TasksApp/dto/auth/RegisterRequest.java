package com.uxelf.TasksApp.dto.auth;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class RegisterRequest {
    @NotEmpty(message = "Username required")
    @Size(min = 3, max = 20, message = "The length of username must be between 3 and 20 characters")
    private String username;

    @NotEmpty(message = "Password required")
    @Size(min = 3, max = 20, message = "The length of password must be between 3 and 20 characters ")
    private String password;
}
