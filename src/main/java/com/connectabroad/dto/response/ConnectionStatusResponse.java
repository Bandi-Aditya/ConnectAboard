package com.connectabroad.dto.response;

public class ConnectionStatusResponse {

    private Long userId;
    private String status; // NONE, PENDING_SENT, PENDING_RECEIVED, CONNECTED, REJECTED
    private Long connectionId;

    public ConnectionStatusResponse() {
    }

    public ConnectionStatusResponse(Long userId, String status, Long connectionId) {
        this.userId = userId;
        this.status = status;
        this.connectionId = connectionId;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Long getConnectionId() {
        return connectionId;
    }

    public void setConnectionId(Long connectionId) {
        this.connectionId = connectionId;
    }
}
