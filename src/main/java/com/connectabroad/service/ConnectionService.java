package com.connectabroad.service;

import com.connectabroad.dto.response.*;
import com.connectabroad.entity.Connection;
import com.connectabroad.entity.ConnectionStatus;
import com.connectabroad.entity.User;
import com.connectabroad.exception.ResourceNotFoundException;
import com.connectabroad.entity.NotificationType;
import com.connectabroad.entity.ReferenceType;
import com.connectabroad.repository.ConnectionRepository;
import com.connectabroad.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class ConnectionService {

    private final ConnectionRepository connectionRepository;
    private final UserRepository userRepository;
    private final ProfileService profileService;
    private final NotificationService notificationService;

    public ConnectionService(ConnectionRepository connectionRepository,
                             UserRepository userRepository,
                             ProfileService profileService,
                             NotificationService notificationService) {
        this.connectionRepository = connectionRepository;
        this.userRepository = userRepository;
        this.profileService = profileService;
        this.notificationService = notificationService;
    }

    @Transactional
    public ConnectionResponse sendRequest(String userEmail, Long targetUserId) {
        User sender = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + userEmail));

        if (sender.getId().equals(targetUserId)) {
            throw new IllegalArgumentException("You cannot connect with yourself.");
        }

        User receiver = userRepository.findById(targetUserId)
                .orElseThrow(() -> new ResourceNotFoundException("Target user not found with id: " + targetUserId));

        Optional<Connection> existingOpt = connectionRepository.findConnectionBetweenUsers(sender.getId(), receiver.getId());
        if (existingOpt.isPresent()) {
            Connection existing = existingOpt.get();
            if (existing.getStatus() == ConnectionStatus.ACCEPTED) {
                throw new IllegalStateException("Users are already connected.");
            }
            if (existing.getStatus() == ConnectionStatus.PENDING) {
                throw new IllegalStateException("Connection request already sent.");
            }
            if (existing.getStatus() == ConnectionStatus.BLOCKED) {
                throw new IllegalStateException("Connection request cannot be sent.");
            }
            if (existing.getStatus() == ConnectionStatus.REJECTED) {
                // Reset rejected request to pending with current user as sender
                existing.setSender(sender);
                existing.setReceiver(receiver);
                existing.setStatus(ConnectionStatus.PENDING);
                Connection saved = connectionRepository.save(existing);

                notificationService.createNotification(
                        receiver,
                        sender,
                        NotificationType.CONNECTION_REQUEST,
                        "Connection Request",
                        sender.getName() + " sent you a connection request.",
                        sender.getId(),
                        ReferenceType.PROFILE
                );

                return mapToConnectionResponse(saved, "Connection request sent.");
            }
        }

        Connection connection = new Connection(sender, receiver, ConnectionStatus.PENDING);
        Connection saved = connectionRepository.save(connection);

        notificationService.createNotification(
                receiver,
                sender,
                NotificationType.CONNECTION_REQUEST,
                "Connection Request",
                sender.getName() + " sent you a connection request.",
                sender.getId(),
                ReferenceType.PROFILE
        );

        return mapToConnectionResponse(saved, "Connection request sent.");
    }

    @Transactional
    public ConnectionResponse acceptRequest(String userEmail, Long connectionId) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + userEmail));

        Connection connection = connectionRepository.findById(connectionId)
                .orElseThrow(() -> new ResourceNotFoundException("Connection request not found with id: " + connectionId));

        if (!connection.getReceiver().getId().equals(user.getId())) {
            throw new IllegalStateException("Only the receiver of a connection request can accept it.");
        }

        if (connection.getStatus() != ConnectionStatus.PENDING) {
            throw new IllegalStateException("Connection request is not pending.");
        }

        connection.setStatus(ConnectionStatus.ACCEPTED);
        Connection updated = connectionRepository.save(connection);

        // Send notification to the original sender
        User sender = connection.getSender();
        notificationService.createNotification(
                sender,
                user,
                NotificationType.CONNECTION_ACCEPTED,
                "Connection Accepted",
                user.getName() + " accepted your connection request.",
                user.getId(),
                ReferenceType.PROFILE
        );

        return mapToConnectionResponse(updated, "Connection request accepted.");
    }

    @Transactional
    public ConnectionResponse rejectRequest(String userEmail, Long connectionId) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + userEmail));

        Connection connection = connectionRepository.findById(connectionId)
                .orElseThrow(() -> new ResourceNotFoundException("Connection request not found with id: " + connectionId));

        if (!connection.getReceiver().getId().equals(user.getId())) {
            throw new IllegalStateException("Only the receiver of a connection request can reject it.");
        }

        if (connection.getStatus() != ConnectionStatus.PENDING) {
            throw new IllegalStateException("Connection request is not pending.");
        }

        connection.setStatus(ConnectionStatus.REJECTED);
        Connection updated = connectionRepository.save(connection);
        return mapToConnectionResponse(updated, "Connection request rejected.");
    }

    @Transactional
    public ConnectionResponse cancelRequest(String userEmail, Long connectionId) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + userEmail));

        Connection connection = connectionRepository.findById(connectionId)
                .orElseThrow(() -> new ResourceNotFoundException("Connection request not found with id: " + connectionId));

        if (!connection.getSender().getId().equals(user.getId())) {
            throw new IllegalStateException("Only the sender can cancel their connection request.");
        }

        if (connection.getStatus() != ConnectionStatus.PENDING) {
            throw new IllegalStateException("Only pending connection requests can be cancelled.");
        }

        ConnectionResponse response = mapToConnectionResponse(connection, "Connection request cancelled.");
        connectionRepository.delete(connection);
        return response;
    }

    @Transactional
    public ConnectionResponse removeConnection(String userEmail, Long connectionId) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + userEmail));

        Connection connection = connectionRepository.findById(connectionId)
                .orElseThrow(() -> new ResourceNotFoundException("Connection not found with id: " + connectionId));

        if (connection.getStatus() != ConnectionStatus.ACCEPTED) {
            throw new IllegalStateException("Only accepted connections can be removed.");
        }

        boolean isSender = connection.getSender().getId().equals(user.getId());
        boolean isReceiver = connection.getReceiver().getId().equals(user.getId());

        if (!isSender && !isReceiver) {
            throw new IllegalStateException("You are not part of this connection.");
        }

        ConnectionResponse response = mapToConnectionResponse(connection, "Connection removed.");
        connectionRepository.delete(connection);
        return response;
    }

    @Transactional(readOnly = true)
    public List<ConnectionRequestResponse> getReceivedRequests(String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + userEmail));

        List<Connection> requests = connectionRepository.findByReceiverIdAndStatusOrderByCreatedAtDesc(user.getId(), ConnectionStatus.PENDING);
        List<ConnectionRequestResponse> responseList = new ArrayList<>();

        for (Connection conn : requests) {
            PublicProfileResponse senderProfile = profileService.getPublicProfileWithContext(userEmail, conn.getSender().getId());
            responseList.add(new ConnectionRequestResponse(conn.getId(), senderProfile, conn.getStatus(), conn.getCreatedAt()));
        }

        return responseList;
    }

    @Transactional(readOnly = true)
    public List<ConnectionRequestResponse> getSentRequests(String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + userEmail));

        List<Connection> requests = connectionRepository.findBySenderIdAndStatusOrderByCreatedAtDesc(user.getId(), ConnectionStatus.PENDING);
        List<ConnectionRequestResponse> responseList = new ArrayList<>();

        for (Connection conn : requests) {
            PublicProfileResponse receiverProfile = profileService.getPublicProfileWithContext(userEmail, conn.getReceiver().getId());
            responseList.add(new ConnectionRequestResponse(conn.getId(), receiverProfile, conn.getStatus(), conn.getCreatedAt()));
        }

        return responseList;
    }

    @Transactional(readOnly = true)
    public List<ConnectedUserResponse> getMyConnections(String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + userEmail));

        List<Connection> connections = connectionRepository.findAcceptedConnectionsForUser(user.getId());
        List<ConnectedUserResponse> responseList = new ArrayList<>();

        for (Connection conn : connections) {
            Long otherUserId = conn.getSender().getId().equals(user.getId())
                    ? conn.getReceiver().getId()
                    : conn.getSender().getId();

            PublicProfileResponse otherProfile = profileService.getPublicProfileWithContext(userEmail, otherUserId);
            responseList.add(new ConnectedUserResponse(conn.getId(), otherProfile, conn.getUpdatedAt()));
        }

        return responseList;
    }

    @Transactional(readOnly = true)
    public ConnectionStatusResponse getConnectionStatus(String userEmail, Long targetUserId) {
        User currentUser = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + userEmail));

        if (currentUser.getId().equals(targetUserId)) {
            return new ConnectionStatusResponse(targetUserId, "NONE", null);
        }

        Optional<Connection> connOpt = connectionRepository.findConnectionBetweenUsers(currentUser.getId(), targetUserId);
        if (connOpt.isEmpty()) {
            return new ConnectionStatusResponse(targetUserId, "NONE", null);
        }

        Connection conn = connOpt.get();
        if (conn.getStatus() == ConnectionStatus.ACCEPTED) {
            return new ConnectionStatusResponse(targetUserId, "CONNECTED", conn.getId());
        }

        if (conn.getStatus() == ConnectionStatus.PENDING) {
            if (conn.getSender().getId().equals(currentUser.getId())) {
                return new ConnectionStatusResponse(targetUserId, "PENDING_SENT", conn.getId());
            } else {
                return new ConnectionStatusResponse(targetUserId, "PENDING_RECEIVED", conn.getId());
            }
        }

        if (conn.getStatus() == ConnectionStatus.REJECTED) {
            return new ConnectionStatusResponse(targetUserId, "REJECTED", conn.getId());
        }

        return new ConnectionStatusResponse(targetUserId, "NONE", conn.getId());
    }

    @Transactional(readOnly = true)
    public long getPendingReceivedCount(String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + userEmail));

        return connectionRepository.countByReceiverIdAndStatus(user.getId(), ConnectionStatus.PENDING);
    }

    @Transactional(readOnly = true)
    public long getConnectionCountForUser(Long userId) {
        return connectionRepository.countAcceptedConnectionsForUser(userId);
    }

    private ConnectionResponse mapToConnectionResponse(Connection connection, String message) {
        return new ConnectionResponse(
                connection.getId(),
                connection.getSender().getId(),
                connection.getReceiver().getId(),
                connection.getStatus(),
                connection.getCreatedAt(),
                connection.getUpdatedAt(),
                message
        );
    }
}
