package com.connectabroad.dto.admin;

import java.time.LocalDateTime;

public class RecentActivityResponse {
    private String id;
    private String type;
    private String description;
    private LocalDateTime timestamp;

    public RecentActivityResponse() {}

    public RecentActivityResponse(String id, String type, String description, LocalDateTime timestamp) {
        this.id = id;
        this.type = type;
        this.description = description;
        this.timestamp = timestamp;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
}
