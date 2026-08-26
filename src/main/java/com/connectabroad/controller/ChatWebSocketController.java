package com.connectabroad.controller;

import com.connectabroad.dto.request.ChatMessageRequest;
import com.connectabroad.dto.response.MessageResponse;
import com.connectabroad.service.ChatService;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.security.Principal;

@Controller
public class ChatWebSocketController {

    private final ChatService chatService;
    private final SimpMessagingTemplate messagingTemplate;

    public ChatWebSocketController(ChatService chatService, SimpMessagingTemplate messagingTemplate) {
        this.chatService = chatService;
        this.messagingTemplate = messagingTemplate;
    }

    @MessageMapping("/chat.send")
    public void processMessage(@Payload ChatMessageRequest request, Principal principal) {
        if (principal == null) {
            throw new IllegalStateException("User is not authenticated");
        }

        MessageResponse messageResponse = chatService.sendMessage(principal.getName(), request);

        // Send real-time message to recipient user's destination: /user/{recipientId}/queue/messages
        messagingTemplate.convertAndSendToUser(
                String.valueOf(request.getRecipientId()),
                "/queue/messages",
                messageResponse
        );

        // Also send back to sender's destination: /user/{senderId}/queue/messages
        messagingTemplate.convertAndSendToUser(
                String.valueOf(messageResponse.getSenderId()),
                "/queue/messages",
                messageResponse
        );
    }
}
