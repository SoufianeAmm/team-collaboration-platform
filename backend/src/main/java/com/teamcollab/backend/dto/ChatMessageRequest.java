package com.teamcollab.backend.dto;

/** The payload a client sends over STOMP to /app/chat.send/{projectId}. */
public record ChatMessageRequest(String content) {
}
