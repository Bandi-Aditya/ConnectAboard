package com.connectabroad.service;

import com.connectabroad.dto.response.AuthorSummaryResponse;
import com.connectabroad.dto.response.NotificationResponse;
import com.connectabroad.dto.response.PageResponse;
import com.connectabroad.dto.response.UnreadCountResponse;
import com.connectabroad.entity.*;
import com.connectabroad.exception.ResourceNotFoundException;
import com.connectabroad.repository.NotificationRepository;
import com.connectabroad.repository.ProfileRepository;
import com.connectabroad.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;
    private final ProfileRepository profileRepository;
    private final SimpMessagingTemplate messagingTemplate;

    public NotificationService(NotificationRepository notificationRepository,
                               UserRepository userRepository,
                               ProfileRepository profileRepository,
                               SimpMessagingTemplate messagingTemplate) {
        this.notificationRepository = notificationRepository;
        this.userRepository = userRepository;
        this.profileRepository = profileRepository;
        this.messagingTemplate = messagingTemplate;
    }

    @Transactional
    public NotificationResponse createNotification(User recipient,
                                                    User actor,
                                                    NotificationType type,
                                                    String title,
                                                    String message,
                                                    Long referenceId,
                                                    ReferenceType referenceType) {
        if (recipient == null) {
            return null;
        }

        // Do not notify user of their own actions
        if (actor != null && actor.getId().equals(recipient.getId())) {
            return null;
        }

        // Duplicate prevention logic for immediate repeat actions (e.g., rapid like/unlike/like)
        if (actor != null && referenceId != null && type == NotificationType.POST_LIKE) {
            LocalDateTime tenMinutesAgo = LocalDateTime.now().minusMinutes(10);
            boolean existsRecent = notificationRepository.existsByRecipientIdAndActorIdAndTypeAndReferenceIdAndCreatedAtAfter(
                    recipient.getId(), actor.getId(), type, referenceId, tenMinutesAgo
            );
            if (existsRecent) {
                // Skip creating duplicate like notification
                return null;
            }
        }

        Notification notification = new Notification(recipient, actor, type, title, message, referenceId, referenceType);
        Notification saved = notificationRepository.save(notification);

        NotificationResponse response = mapToNotificationResponse(saved);

        // Dispatch real-time WebSocket notification to recipient's queue
        try {
            messagingTemplate.convertAndSendToUser(
                    String.valueOf(recipient.getId()),
                    "/queue/notifications",
                    response
            );
        } catch (Exception e) {
            // Log warning but do not break transaction if WebSocket fails
            System.err.println("Failed to send real-time WebSocket notification: " + e.getMessage());
        }

        return response;
    }

    @Transactional(readOnly = true)
    public PageResponse<NotificationResponse> getUserNotifications(String userEmail, Pageable pageable) {
        User user = getUserByEmail(userEmail);
        Page<Notification> page = notificationRepository.findByRecipientIdOrderByCreatedAtDesc(user.getId(), pageable);

        List<NotificationResponse> content = page.getContent().stream()
                .map(this::mapToNotificationResponse)
                .toList();

        return new PageResponse<>(
                content,
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages(),
                page.isLast()
        );
    }

    @Transactional(readOnly = true)
    public UnreadCountResponse getUnreadCount(String userEmail) {
        User user = getUserByEmail(userEmail);
        long unreadCount = notificationRepository.countByRecipientIdAndIsReadFalse(user.getId());
        return new UnreadCountResponse(unreadCount);
    }

    @Transactional
    public NotificationResponse markAsRead(String userEmail, Long notificationId) {
        User user = getUserByEmail(userEmail);
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new ResourceNotFoundException("Notification not found with id: " + notificationId));

        if (!notification.getRecipient().getId().equals(user.getId())) {
            throw new AccessDeniedException("You are not authorized to mark this notification as read.");
        }

        if (!notification.isRead()) {
            notification.setRead(true);
            notification = notificationRepository.save(notification);
        }

        return mapToNotificationResponse(notification);
    }

    @Transactional
    public void markAllAsRead(String userEmail) {
        User user = getUserByEmail(userEmail);
        notificationRepository.markAllAsReadForRecipient(user.getId());
    }

    @Transactional
    public void deleteNotification(String userEmail, Long notificationId) {
        User user = getUserByEmail(userEmail);
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new ResourceNotFoundException("Notification not found with id: " + notificationId));

        if (!notification.getRecipient().getId().equals(user.getId())) {
            throw new AccessDeniedException("You are not authorized to delete this notification.");
        }

        notificationRepository.delete(notification);
    }

    private NotificationResponse mapToNotificationResponse(Notification notification) {
        AuthorSummaryResponse actorSummary = notification.getActor() != null ? buildAuthorSummary(notification.getActor()) : null;

        return new NotificationResponse(
                notification.getId(),
                actorSummary,
                notification.getType(),
                notification.getTitle(),
                notification.getMessage(),
                notification.getReferenceId(),
                notification.getReferenceType(),
                notification.isRead(),
                notification.getCreatedAt()
        );
    }

    private AuthorSummaryResponse buildAuthorSummary(User user) {
        Optional<Profile> profOpt = profileRepository.findByUserId(user.getId());
        Profile prof = profOpt.orElse(null);

        String photo = prof != null ? prof.getProfilePhoto() : null;
        String profession = prof != null ? prof.getProfession() : "Community Member";
        String city = prof != null ? prof.getCurrentCity() : null;
        String country = prof != null ? prof.getCurrentCountry() : null;
        String college = (prof != null && prof.getCollege() != null) ? prof.getCollege().getName() : null;

        return new AuthorSummaryResponse(
                user.getId(),
                user.getName(),
                photo,
                profession,
                city,
                country,
                college,
                user.getUserType()
        );
    }

    private User getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + email));
    }
}
