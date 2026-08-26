package com.connectabroad.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class ChatMessageRequest {

    @NotNull(message = "Conversation ID is required")
    private Long conversationId;

    @NotNull(message = "Recipient ID is required")
    private Long recipientId;

    @NotBlank(message = "Message content cannot be blank")
    private String content;

    public ChatMessageRequest() {
    }

    public ChatMessageRequest(Long conversationId, Long recipientId, String content) {
        this.conversationId = conversationId;
        this.recipientId = recipientId;
        this.content = content;
    }

    public Long getConversationId() {
        return conversationId;
    }

    public void setConversationId(Long conversationId) {
        this.conversationId = conversationId;
    }

    public Long getRecipientId() {
        return recipientId;
    }

    public void setRecipientId(Long recipientId) {
        this.recipientId = recipientId;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}
