package com.connectabroad.repository;

import com.connectabroad.entity.Notification;
import com.connectabroad.entity.NotificationType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {

    Page<Notification> findByRecipientIdOrderByCreatedAtDesc(Long recipientId, Pageable pageable);

    long countByRecipientIdAndIsReadFalse(Long recipientId);

    Optional<Notification> findByIdAndRecipientId(Long id, Long recipientId);

    @Modifying
    @Query("UPDATE Notification n SET n.isRead = true WHERE n.recipient.id = :recipientId AND n.isRead = false")
    int markAllAsReadForRecipient(@Param("recipientId") Long recipientId);

    boolean existsByRecipientIdAndActorIdAndTypeAndReferenceIdAndCreatedAtAfter(
            Long recipientId,
            Long actorId,
            NotificationType type,
            Long referenceId,
            LocalDateTime afterTime
    );

    boolean existsByRecipientIdAndActorIdAndTypeAndReferenceIdAndIsReadFalse(
            Long recipientId,
            Long actorId,
            NotificationType type,
            Long referenceId
    );
}
