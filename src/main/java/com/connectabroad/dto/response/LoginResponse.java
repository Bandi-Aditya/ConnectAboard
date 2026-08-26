package com.connectabroad.dto.response;

import com.connectabroad.entity.Role;
import com.connectabroad.entity.UserType;

public class LoginResponse {

    private String token;
    private String tokenType = "Bearer";
    private Long userId;
    private String name;
    private String email;
    private Role role;
    private UserType userType;

    public LoginResponse() {
    }

    public LoginResponse(String token, Long userId, String name, String email, Role role, UserType userType) {
        this.token = token;
        this.userId = userId;
        this.name = name;
        this.email = email;
        this.role = role;
        this.userType = userType;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getTokenType() {
        return tokenType;
    }

    public void setTokenType(String tokenType) {
        this.tokenType = tokenType;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
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
}
