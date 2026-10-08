package com.teamcollab.backend.dto;

import com.teamcollab.backend.entity.Notification;
import com.teamcollab.backend.entity.NotificationType;

import java.time.Instant;

public record NotificationResponse(
        Long id,
        NotificationType type,
        String content,
        boolean isRead,
        Long relatedProjectId,
        Long relatedTaskId,
        Instant createdAt
) {
    public static NotificationResponse from(Notification n) {
        return new NotificationResponse(
                n.getId(),
                n.getType(),
                n.getContent(),
                n.isRead(),
                n.getRelatedProject() == null ? null : n.getRelatedProject().getId(),
                n.getRelatedTask() == null ? null : n.getRelatedTask().getId(),
                n.getCreatedAt()
        );
    }
}
