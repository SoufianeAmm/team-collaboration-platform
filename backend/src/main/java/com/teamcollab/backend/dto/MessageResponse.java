package com.teamcollab.backend.dto;

import com.teamcollab.backend.entity.Message;

import java.time.Instant;

/** Used both for REST chat history and the payload broadcast over /topic/projects/{id}/chat. */
public record MessageResponse(
        Long id,
        String content,
        UserResponse sender,
        Long projectId,
        Instant createdAt
) {
    public static MessageResponse from(Message message) {
        return new MessageResponse(
                message.getId(),
                message.getContent(),
                UserResponse.from(message.getSender()),
                message.getProject().getId(),
                message.getCreatedAt()
        );
    }
}
