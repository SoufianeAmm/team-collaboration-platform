package com.teamcollab.backend.dto;

import com.teamcollab.backend.entity.Comment;

import java.time.Instant;

public record CommentResponse(
        Long id,
        String content,
        UserResponse author,
        Long taskId,
        Instant createdAt,
        Instant updatedAt
) {
    public static CommentResponse from(Comment comment) {
        return new CommentResponse(
                comment.getId(),
                comment.getContent(),
                UserResponse.from(comment.getAuthor()),
                comment.getTask().getId(),
                comment.getCreatedAt(),
                comment.getUpdatedAt()
        );
    }
}
