package com.connectabroad.controller;

import com.connectabroad.dto.response.NotificationResponse;
import com.connectabroad.dto.response.PageResponse;
import com.connectabroad.dto.response.UnreadCountResponse;
import com.connectabroad.service.NotificationService;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @GetMapping
    public ResponseEntity<PageResponse<NotificationResponse>> getUserNotifications(
            Authentication authentication,
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "20") int size) {
        String userEmail = authentication.getName();
        Pageable pageable = PageRequest.of(page, size);
        PageResponse<NotificationResponse> response = notificationService.getUserNotifications(userEmail, pageable);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/unread-count")
    public ResponseEntity<UnreadCountResponse> getUnreadCount(Authentication authentication) {
        String userEmail = authentication.getName();
        UnreadCountResponse response = notificationService.getUnreadCount(userEmail);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{id}/read")
    public ResponseEntity<NotificationResponse> markAsRead(
            Authentication authentication,
            @PathVariable("id") Long notificationId) {
        String userEmail = authentication.getName();
        NotificationResponse response = notificationService.markAsRead(userEmail, notificationId);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/read-all")
    public ResponseEntity<Void> markAllAsRead(Authentication authentication) {
        String userEmail = authentication.getName();
        notificationService.markAllAsRead(userEmail);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteNotification(
            Authentication authentication,
            @PathVariable("id") Long notificationId) {
        String userEmail = authentication.getName();
        notificationService.deleteNotification(userEmail, notificationId);
        return ResponseEntity.noContent().build();
    }
}
