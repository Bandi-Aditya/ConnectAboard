package com.connectabroad.dto.response;

import com.connectabroad.entity.Role;
import com.connectabroad.entity.User;
import com.connectabroad.entity.UserType;

import java.time.LocalDateTime;

public class UserResponse {

    private Long id;
    private String name;
    private String email;
    private Role role;
    private UserType userType;
    private LocalDateTime createdAt;
    private boolean enabled;

    public UserResponse() {
    }

    public UserResponse(Long id, String name, String email, Role role, UserType userType, LocalDateTime createdAt, boolean enabled) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.role = role;
        this.userType = userType;
        this.createdAt = createdAt;
        this.enabled = enabled;
    }

    public static UserResponse fromEntity(User user) {
        return new UserResponse(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getRole(),
                user.getUserType(),
                user.getCreatedAt(),
                user.isEnabled()
        );
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    public UserType getUserType() {
        return userType;
    }

    public void setUserType(UserType userType) {
        this.userType = userType;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
}
