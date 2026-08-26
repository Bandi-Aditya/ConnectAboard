package com.connectabroad.repository;

import com.connectabroad.entity.Message;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MessageRepository extends JpaRepository<Message, Long> {

    Page<Message> findByConversationIdAndDeletedFalseOrderByCreatedAtAsc(Long conversationId, Pageable pageable);

    @Query("SELECT COUNT(m) FROM Message m WHERE m.conversation.id = :conversationId " +
           "AND m.sender.id <> :userId AND m.readAt IS NULL AND m.deleted = false")
    long countUnreadMessagesForUserInConversation(@Param("conversationId") Long conversationId, @Param("userId") Long userId);

    @Query("SELECT m FROM Message m WHERE m.conversation.id = :conversationId AND m.deleted = false " +
           "ORDER BY m.createdAt DESC LIMIT 1")
    Optional<Message> findLatestMessageByConversationId(@Param("conversationId") Long conversationId);

    @Query("SELECT m FROM Message m WHERE m.conversation.id = :conversationId AND m.sender.id <> :userId " +
           "AND m.readAt IS NULL AND m.deleted = false")
    List<Message> findUnreadMessagesForUserInConversation(@Param("conversationId") Long conversationId, @Param("userId") Long userId);
}
