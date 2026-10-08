package com.teamcollab.backend.dto;

import com.teamcollab.backend.entity.Project;
import com.teamcollab.backend.entity.ProjectStatus;

import java.time.Instant;
import java.util.List;

public record ProjectResponse(
        Long id,
        String name,
        String description,
        UserResponse owner,
        ProjectStatus status,
        List<UserResponse> members,
        TaskStatsResponse taskStats,
        Instant createdAt,
        Instant updatedAt
) {
    public static ProjectResponse from(Project project) {
        return new ProjectResponse(
                project.getId(),
                project.getName(),
                project.getDescription(),
                UserResponse.from(project.getOwner()),
                project.getStatus(),
                project.getMembers().stream().map(UserResponse::from).toList(),
                TaskStatsResponse.from(project.getTasks()),
                project.getCreatedAt(),
                project.getUpdatedAt()
        );
    }
}
