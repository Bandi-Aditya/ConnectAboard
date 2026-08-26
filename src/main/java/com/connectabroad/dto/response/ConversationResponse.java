package com.connectabroad.dto.response;

import java.time.LocalDateTime;

public class ConversationResponse {

    private Long id;
    private PublicProfileResponse otherUser;
    private String lastMessage;
    private LocalDateTime lastMessageAt;
    private long unreadCount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public ConversationResponse() {
    }

    public ConversationResponse(Long id, PublicProfileResponse otherUser, String lastMessage,
                                LocalDateTime lastMessageAt, long unreadCount,
                                LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.otherUser = otherUser;
        this.lastMessage = lastMessage;
        this.lastMessageAt = lastMessageAt;
        this.unreadCount = unreadCount;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public PublicProfileResponse getOtherUser() {
        return otherUser;
    }

    public void setOtherUser(PublicProfileResponse otherUser) {
        this.otherUser = otherUser;
    }

    public String getLastMessage() {
        return lastMessage;
    }

    public void setLastMessage(String lastMessage) {
        this.lastMessage = lastMessage;
    }

    public LocalDateTime getLastMessageAt() {
        return lastMessageAt;
    }

    public void setLastMessageAt(LocalDateTime lastMessageAt) {
        this.lastMessageAt = lastMessageAt;
    }

    public long getUnreadCount() {
        return unreadCount;
    }

    public void setUnreadCount(long unreadCount) {
        this.unreadCount = unreadCount;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
