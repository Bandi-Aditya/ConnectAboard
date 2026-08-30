package com.connectabroad.dto.response;

import com.connectabroad.entity.NotificationType;
import com.connectabroad.entity.ReferenceType;

import java.time.LocalDateTime;

public class NotificationResponse {

    private Long id;
    private AuthorSummaryResponse actor;
    private NotificationType type;
    private String title;
    private String message;
    private Long referenceId;
    private ReferenceType referenceType;
    private boolean isRead;
    private LocalDateTime createdAt;

    public NotificationResponse() {
    }

    public NotificationResponse(Long id, AuthorSummaryResponse actor, NotificationType type,
                                String title, String message, Long referenceId,
                                ReferenceType referenceType, boolean isRead, LocalDateTime createdAt) {
        this.id = id;
        this.actor = actor;
        this.type = type;
        this.title = title;
        this.message = message;
        this.referenceId = referenceId;
        this.referenceType = referenceType;
        this.isRead = isRead;
        this.createdAt = createdAt;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public AuthorSummaryResponse getActor() {
        return actor;
    }

    public void setActor(AuthorSummaryResponse actor) {
        this.actor = actor;
    }

    public NotificationType getType() {
        return type;
    }

    public void setType(NotificationType type) {
        this.type = type;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Long getReferenceId() {
        return referenceId;
    }

    public void setReferenceId(Long referenceId) {
        this.referenceId = referenceId;
    }

    public ReferenceType getReferenceType() {
        return referenceType;
    }

    public void setReferenceType(ReferenceType referenceType) {
        this.referenceType = referenceType;
    }

    public boolean isRead() {
        return isRead;
    }

    public void setRead(boolean read) {
        isRead = read;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
