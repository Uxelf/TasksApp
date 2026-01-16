package com.uxelf.TasksApp.service;

import com.uxelf.TasksApp.entity.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.Map;
import java.util.UUID;

@Service
public class JwtService {
    private static final SecretKey key = Keys.secretKeyFor(SignatureAlgorithm.HS256);
    private static final long EXPIRATION = 7 * 24 * 1000 * 60 * 60; // 7 dias

    public String generateToken(User user){
        Map<String, Object> claims = Map.of(
                "id", user.getId(),
                "username", user.getUsername()
        );

        return Jwts.builder()
                .setClaims(claims)
                .setSubject(user.getUsername())
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION))
                .signWith(key)
                .compact();
    }

    public Claims validateToken(String token){
        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    public UUID getUserId(String token){
        return validateToken(token).get("id", UUID.class);
    }

    public String getUsername(String token){
        return validateToken(token).get("username", String.class);
    }
}
