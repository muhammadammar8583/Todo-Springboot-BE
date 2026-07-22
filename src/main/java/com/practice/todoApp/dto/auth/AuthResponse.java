package com.practice.todoApp.dto.auth;

import com.practice.todoApp.model.Role;

public class AuthResponse {
    private String token;
    private String username;
    private Role role;
    private String email;

    public AuthResponse() {
    }

    public AuthResponse(String token, String username, Role role, String email) {
        this.token = token;
        this.username = username;
        this.role = role;
        this.email = email;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}
