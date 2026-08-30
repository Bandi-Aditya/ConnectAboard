package com.connectabroad.repository;

import com.connectabroad.entity.Connection;
import com.connectabroad.entity.ConnectionStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ConnectionRepository extends JpaRepository<Connection, Long> {

    @Query("SELECT c FROM Connection c WHERE (c.sender.id = :user1Id AND c.receiver.id = :user2Id) OR (c.sender.id = :user2Id AND c.receiver.id = :user1Id)")
    Optional<Connection> findConnectionBetweenUsers(@Param("user1Id") Long user1Id, @Param("user2Id") Long user2Id);

    List<Connection> findByReceiverIdAndStatusOrderByCreatedAtDesc(Long receiverId, ConnectionStatus status);

    List<Connection> findBySenderIdAndStatusOrderByCreatedAtDesc(Long senderId, ConnectionStatus status);

    @Query("SELECT c FROM Connection c WHERE (c.sender.id = :userId OR c.receiver.id = :userId) AND c.status = com.connectabroad.entity.ConnectionStatus.ACCEPTED ORDER BY c.updatedAt DESC")
    List<Connection> findAcceptedConnectionsForUser(@Param("userId") Long userId);

    long countByReceiverIdAndStatus(Long receiverId, ConnectionStatus status);

    long countByStatus(ConnectionStatus status);

    @Query("SELECT COUNT(c) FROM Connection c WHERE (c.sender.id = :userId OR c.receiver.id = :userId) AND c.status = com.connectabroad.entity.ConnectionStatus.ACCEPTED")
    long countAcceptedConnectionsForUser(@Param("userId") Long userId);

    @Query("SELECT CASE WHEN c.sender.id = :userId THEN c.receiver.id ELSE c.sender.id END FROM Connection c WHERE (c.sender.id = :userId OR c.receiver.id = :userId) AND c.status = com.connectabroad.entity.ConnectionStatus.ACCEPTED")
    List<Long> findConnectedUserIds(@Param("userId") Long userId);

    @Query("SELECT CAST(c.createdAt AS date) as date, COUNT(c) as count " +
           "FROM Connection c WHERE c.createdAt >= :startDate " +
           "GROUP BY CAST(c.createdAt AS date) ORDER BY CAST(c.createdAt AS date)")
    List<Object[]> countNewConnectionsPerDay(@Param("startDate") LocalDateTime startDate);
}
