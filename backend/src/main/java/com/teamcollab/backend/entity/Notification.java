package com.teamcollab.backend.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.time.Instant;

@Entity
@Table(name = "notifications")
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "recipient_id", nullable = false)
    private User recipient;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private NotificationType type;

    @Column(nullable = false, length = 1000)
    private String content;

    @Column(nullable = false)
    private boolean isRead = false;

    // Optional links back to what triggered the notification, so the frontend can navigate there.
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "related_project_id")
    private Project relatedProject;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "related_task_id")
    private Task relatedTask;

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    protected Notification() {
    }

    public Notification(User recipient, NotificationType type, String content,
                         Project relatedProject, Task relatedTask) {
        this.recipient = recipient;
        this.type = type;
        this.content = content;
        this.relatedProject = relatedProject;
        this.relatedTask = relatedTask;
    }

    @jakarta.persistence.PrePersist
    protected void onCreate() {
        this.createdAt = Instant.now();
    }

    public Long getId() {
        return id;
    }

    public User getRecipient() {
        return recipient;
    }

    public NotificationType getType() {
        return type;
    }

    public String getContent() {
        return content;
    }

    public boolean isRead() {
        return isRead;
    }

    public void markAsRead() {
        this.isRead = true;
    }

    public Project getRelatedProject() {
        return relatedProject;
    }

    public Task getRelatedTask() {
        return relatedTask;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
