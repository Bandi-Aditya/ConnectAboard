package com.connectabroad.dto.response;

import com.connectabroad.entity.Message;
import com.connectabroad.entity.MessageType;

import java.time.LocalDateTime;

public class MessageResponse {

    private Long id;
    private Long conversationId;
    private Long senderId;
    private String senderName;
    private String content;
    private MessageType messageType;
    private LocalDateTime readAt;
    private boolean deleted;
    private LocalDateTime createdAt;

    public MessageResponse() {
    }

    public MessageResponse(Long id, Long conversationId, Long senderId, String senderName,
                           String content, MessageType messageType, LocalDateTime readAt,
                           boolean deleted, LocalDateTime createdAt) {
        this.id = id;
        this.conversationId = conversationId;
        this.senderId = senderId;
        this.senderName = senderName;
        this.content = content;
        this.messageType = messageType;
        this.readAt = readAt;
        this.deleted = deleted;
        this.createdAt = createdAt;
    }

    public static MessageResponse fromEntity(Message message) {
        return new MessageResponse(
                message.getId(),
                message.getConversation().getId(),
                message.getSender().getId(),
                message.getSender().getName(),
                message.isDeleted() ? "[Message deleted]" : message.getContent(),
                message.getMessageType(),
                message.getReadAt(),
                message.isDeleted(),
                message.getCreatedAt()
        );
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getConversationId() {
        return conversationId;
    }

    public void setConversationId(Long conversationId) {
        this.conversationId = conversationId;
    }

    public Long getSenderId() {
        return senderId;
    }

    public void setSenderId(Long senderId) {
        this.senderId = senderId;
    }

    public String getSenderName() {
        return senderName;
    }

    public void setSenderName(String senderName) {
        this.senderName = senderName;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public MessageType getMessageType() {
        return messageType;
    }

    public void setMessageType(MessageType messageType) {
        this.messageType = messageType;
    }

    public LocalDateTime getReadAt() {
        return readAt;
    }

    public void setReadAt(LocalDateTime readAt) {
        this.readAt = readAt;
    }

    public boolean isDeleted() {
        return deleted;
    }

    public void setDeleted(boolean deleted) {
        this.deleted = deleted;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
