package com.connectabroad.dto.response;

import com.connectabroad.entity.ConnectionStatus;

import java.time.LocalDateTime;

public class ConnectionRequestResponse {

    private Long connectionId;
    private PublicProfileResponse user;
    private ConnectionStatus status;
    private LocalDateTime createdAt;

    public ConnectionRequestResponse() {
    }

    public ConnectionRequestResponse(Long connectionId, PublicProfileResponse user, ConnectionStatus status, LocalDateTime createdAt) {
        this.connectionId = connectionId;
        this.user = user;
        this.status = status;
        this.createdAt = createdAt;
    }

    public Long getConnectionId() {
        return connectionId;
    }

    public void setConnectionId(Long connectionId) {
        this.connectionId = connectionId;
    }

    public PublicProfileResponse getUser() {
        return user;
    }

    public void setUser(PublicProfileResponse user) {
        this.user = user;
    }

    public ConnectionStatus getStatus() {
        return status;
    }

    public void setStatus(ConnectionStatus status) {
        this.status = status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
