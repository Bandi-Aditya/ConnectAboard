package com.connectabroad.service;

import com.connectabroad.dto.request.ChatMessageRequest;
import com.connectabroad.dto.response.ConversationResponse;
import com.connectabroad.dto.response.MessageResponse;
import com.connectabroad.dto.response.PublicProfileResponse;
import com.connectabroad.entity.Connection;
import com.connectabroad.entity.ConnectionStatus;
import com.connectabroad.entity.Conversation;
import com.connectabroad.entity.Message;
import com.connectabroad.entity.User;
import com.connectabroad.repository.ConnectionRepository;
import com.connectabroad.repository.ConversationRepository;
import com.connectabroad.repository.MessageRepository;
import com.connectabroad.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class ChatService {

    private final ConversationRepository conversationRepository;
    private final MessageRepository messageRepository;
    private final ConnectionRepository connectionRepository;
    private final UserRepository userRepository;
    private final ProfileService profileService;

    public ChatService(ConversationRepository conversationRepository,
                       MessageRepository messageRepository,
                       ConnectionRepository connectionRepository,
                       UserRepository userRepository,
                       ProfileService profileService) {
        this.conversationRepository = conversationRepository;
        this.messageRepository = messageRepository;
        this.connectionRepository = connectionRepository;
        this.userRepository = userRepository;
        this.profileService = profileService;
    }

    @Transactional
    public ConversationResponse getOrCreateConversation(String currentUserEmail, Long targetUserId) {
        User currentUser = getUserByEmail(currentUserEmail);

        if (currentUser.getId().equals(targetUserId)) {
            throw new IllegalArgumentException("You cannot start a conversation with yourself");
        }

        User targetUser = userRepository.findById(targetUserId)
                .orElseThrow(() -> new IllegalArgumentException("User not found with id: " + targetUserId));

        // Rule: Only CONNECTED users can start or participate in private chat
        Optional<Connection> connectionOpt = connectionRepository.findConnectionBetweenUsers(currentUser.getId(), targetUserId);
        if (connectionOpt.isEmpty() || connectionOpt.get().getStatus() != ConnectionStatus.ACCEPTED) {
            throw new AccessDeniedException("You must be connected with this user before starting a conversation.");
        }

        Optional<Conversation> existingConv = conversationRepository.findConversationBetweenUsers(currentUser.getId(), targetUserId);
        Conversation conversation;

        if (existingConv.isPresent()) {
            conversation = existingConv.get();
        } else {
            // Normalize participant order (lower ID as participantOne, higher as participantTwo)
            User p1 = currentUser.getId() < targetUserId ? currentUser : targetUser;
            User p2 = currentUser.getId() < targetUserId ? targetUser : currentUser;
            conversation = conversationRepository.save(new Conversation(p1, p2));
        }

        return buildConversationResponse(conversation, currentUser.getId());
    }

    @Transactional(readOnly = true)
    public List<ConversationResponse> getUserConversations(String currentUserEmail) {
        User currentUser = getUserByEmail(currentUserEmail);
        List<Conversation> conversations = conversationRepository.findAllConversationsForUser(currentUser.getId());

        List<ConversationResponse> responses = new ArrayList<>();
        for (Conversation conv : conversations) {
            responses.add(buildConversationResponse(conv, currentUser.getId()));
        }
        return responses;
    }

    @Transactional
    public Page<MessageResponse> getConversationMessages(String currentUserEmail, Long conversationId, Pageable pageable) {
        User currentUser = getUserByEmail(currentUserEmail);

        Conversation conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new IllegalArgumentException("Conversation not found"));

        if (!conversation.isParticipant(currentUser.getId())) {
            throw new AccessDeniedException("You are not a participant in this conversation");
        }

        // Mark unread messages sent to current user as read
        List<Message> unreadMessages = messageRepository.findUnreadMessagesForUserInConversation(conversationId, currentUser.getId());
        if (!unreadMessages.isEmpty()) {
            LocalDateTime now = LocalDateTime.now();
            for (Message msg : unreadMessages) {
                msg.setReadAt(now);
            }
            messageRepository.saveAll(unreadMessages);
        }

        Page<Message> messagePage = messageRepository.findByConversationIdAndDeletedFalseOrderByCreatedAtAsc(conversationId, pageable);
        return messagePage.map(MessageResponse::fromEntity);
    }

    @Transactional
    public MessageResponse sendMessage(String senderEmail, ChatMessageRequest request) {
        User sender = getUserByEmail(senderEmail);

        Conversation conversation = conversationRepository.findById(request.getConversationId())
                .orElseThrow(() -> new IllegalArgumentException("Conversation not found"));

        if (!conversation.isParticipant(sender.getId())) {
            throw new AccessDeniedException("You are not a participant in this conversation");
        }

        User otherUser = conversation.getOtherParticipant(sender.getId());
        if (!otherUser.getId().equals(request.getRecipientId())) {
            throw new IllegalArgumentException("Recipient ID does not match conversation recipient");
        }

        // Rule check connection status
        Optional<Connection> connectionOpt = connectionRepository.findConnectionBetweenUsers(sender.getId(), otherUser.getId());
        if (connectionOpt.isEmpty() || connectionOpt.get().getStatus() != ConnectionStatus.ACCEPTED) {
            throw new AccessDeniedException("You must be connected with this user before starting a conversation.");
        }

        Message message = new Message(conversation, sender, request.getContent());
        Message savedMessage = messageRepository.save(message);

        conversation.setUpdatedAt(LocalDateTime.now());
        conversationRepository.save(conversation);

        return MessageResponse.fromEntity(savedMessage);
    }

    @Transactional
    public MessageResponse deleteMessage(String currentUserEmail, Long messageId) {
        User currentUser = getUserByEmail(currentUserEmail);

        Message message = messageRepository.findById(messageId)
                .orElseThrow(() -> new IllegalArgumentException("Message not found"));

        if (!message.getSender().getId().equals(currentUser.getId())) {
            throw new AccessDeniedException("You can only delete your own messages");
        }

        message.setDeleted(true);
        Message updated = messageRepository.save(message);
        return MessageResponse.fromEntity(updated);
    }

    private ConversationResponse buildConversationResponse(Conversation conv, Long currentUserId) {
        User otherUser = conv.getOtherParticipant(currentUserId);
        PublicProfileResponse otherProfile = profileService.getPublicProfile(otherUser.getId());

        Optional<Message> latestMsgOpt = messageRepository.findLatestMessageByConversationId(conv.getId());
        String lastMessage = latestMsgOpt.map(m -> m.isDeleted() ? "[Message deleted]" : m.getContent()).orElse("No messages yet");
        LocalDateTime lastMessageAt = latestMsgOpt.map(Message::getCreatedAt).orElse(conv.getCreatedAt());

        long unreadCount = messageRepository.countUnreadMessagesForUserInConversation(conv.getId(), currentUserId);

        return new ConversationResponse(
                conv.getId(),
                otherProfile,
                lastMessage,
                lastMessageAt,
                unreadCount,
                conv.getCreatedAt(),
                conv.getUpdatedAt()
        );
    }

    private User getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found with email: " + email));
    }
}
