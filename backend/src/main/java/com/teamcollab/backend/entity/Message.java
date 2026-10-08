package com.teamcollab.backend.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.time.Instant;

/**
 * A project team-chat message. Persisted here for history; delivered live via WebSockets in Phase 10.
 */
@Entity
@Table(name = "messages")
public class Message {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 2000)
    private String content;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sender_id", nullable = false)
    private User sender;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id", nullable = false)
    private Project project;

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    protected Message() {
    }

    public Message(String content, User sender, Project project) {
        this.content = content;
        this.sender = sender;
        this.project = project;
    }

    @jakarta.persistence.PrePersist
    protected void onCreate() {
        this.createdAt = Instant.now();
    }

    public Long getId() {
        return id;
    }

    public String getContent() {
        return content;
    }

    public User getSender() {
        return sender;
    }

    public Project getProject() {
        return project;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
