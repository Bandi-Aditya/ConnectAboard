package com.connectabroad.controller;

import com.connectabroad.dto.request.ChatMessageRequest;
import com.connectabroad.dto.response.ConversationResponse;
import com.connectabroad.dto.response.MessageResponse;
import com.connectabroad.service.ChatService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/conversations")
public class ChatController {

    private final ChatService chatService;

    public ChatController(ChatService chatService) {
        this.chatService = chatService;
    }

    @GetMapping
    public ResponseEntity<List<ConversationResponse>> getUserConversations(Authentication authentication) {
        List<ConversationResponse> conversations = chatService.getUserConversations(authentication.getName());
        return ResponseEntity.ok(conversations);
    }

    @PostMapping("/{userId}")
    public ResponseEntity<ConversationResponse> getOrCreateConversation(@PathVariable Long userId,
                                                                          Authentication authentication) {
        ConversationResponse conversation = chatService.getOrCreateConversation(authentication.getName(), userId);
        return ResponseEntity.ok(conversation);
    }

    @GetMapping("/{conversationId}/messages")
    public ResponseEntity<Page<MessageResponse>> getConversationMessages(@PathVariable Long conversationId,
                                                                          @RequestParam(defaultValue = "0") int page,
                                                                          @RequestParam(defaultValue = "30") int size,
                                                                          Authentication authentication) {
        PageRequest pageable = PageRequest.of(page, size, Sort.by("createdAt").ascending());
        Page<MessageResponse> messages = chatService.getConversationMessages(authentication.getName(), conversationId, pageable);
        return ResponseEntity.ok(messages);
    }

    @PostMapping("/{conversationId}/messages")
    public ResponseEntity<MessageResponse> sendMessageREST(@PathVariable Long conversationId,
                                                            @Valid @RequestBody ChatMessageRequest request,
                                                            Authentication authentication) {
        request.setConversationId(conversationId);
        MessageResponse message = chatService.sendMessage(authentication.getName(), request);
        return ResponseEntity.ok(message);
    }

    @DeleteMapping("/messages/{messageId}")
    public ResponseEntity<MessageResponse> deleteMessage(@PathVariable Long messageId,
                                                          Authentication authentication) {
        MessageResponse deletedMessage = chatService.deleteMessage(authentication.getName(), messageId);
        return ResponseEntity.ok(deletedMessage);
    }
}
