package com.connectabroad.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(
    name = "conversations",
    uniqueConstraints = {
        @UniqueConstraint(columnNames = {"participant_one_id", "participant_two_id"})
    },
    indexes = {
        @Index(name = "idx_conv_p1", columnList = "participant_one_id"),
        @Index(name = "idx_conv_p2", columnList = "participant_two_id"),
        @Index(name = "idx_conv_updated", columnList = "updated_at DESC")
    }
)
public class Conversation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "participant_one_id", nullable = false)
    private User participantOne;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "participant_two_id", nullable = false)
    private User participantTwo;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    public Conversation() {
    }

    public Conversation(User participantOne, User participantTwo) {
        this.participantOne = participantOne;
        this.participantTwo = participantTwo;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public User getParticipantOne() {
        return participantOne;
    }

    public void setParticipantOne(User participantOne) {
        this.participantOne = participantOne;
    }

    public User getParticipantTwo() {
        return participantTwo;
    }

    public void setParticipantTwo(User participantTwo) {
        this.participantTwo = participantTwo;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public boolean isParticipant(Long userId) {
        if (userId == null) return false;
        return (participantOne != null && userId.equals(participantOne.getId())) ||
               (participantTwo != null && userId.equals(participantTwo.getId()));
    }

    public User getOtherParticipant(Long userId) {
        if (participantOne != null && userId.equals(participantOne.getId())) {
            return participantTwo;
        }
        return participantOne;
    }
}
