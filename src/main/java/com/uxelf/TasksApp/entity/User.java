package com.uxelf.TasksApp.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Table(name = "users")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User {

    @Id
    @GeneratedValue
    @Column(columnDefinition = "uuid")
    private UUID id;

    @Column(nullable = false)
    private String username;

    @Column(nullable = false)
    private String password;

    // Constructor público original (para uso en producción)
    public User(String username, String password) {
        this.username = username;
        this.password = password;
    }

    // Constructor público para testing - permite setear el ID manualmente
    public User(UUID id, String username, String password) {
        this.id = id;
        this.username = username;
        this.password = password;
    }
}