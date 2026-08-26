package com.connectabroad.dto.response;

import java.time.LocalDateTime;

public class ConnectedUserResponse {

    private Long connectionId;
    private PublicProfileResponse user;
    private LocalDateTime connectedAt;

    public ConnectedUserResponse() {
    }

    public ConnectedUserResponse(Long connectionId, PublicProfileResponse user, LocalDateTime connectedAt) {
        this.connectionId = connectionId;
        this.user = user;
        this.connectedAt = connectedAt;
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

    public LocalDateTime getConnectedAt() {
        return connectedAt;
    }

    public void setConnectedAt(LocalDateTime connectedAt) {
        this.connectedAt = connectedAt;
    }
}
