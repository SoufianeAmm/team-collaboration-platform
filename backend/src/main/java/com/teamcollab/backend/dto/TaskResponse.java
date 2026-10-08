package com.teamcollab.backend.dto;

import com.teamcollab.backend.entity.Task;
import com.teamcollab.backend.entity.TaskPriority;
import com.teamcollab.backend.entity.TaskStatus;

import java.time.Instant;
import java.time.LocalDate;

public record TaskResponse(
        Long id,
        String title,
        String description,
        TaskStatus status,
        TaskPriority priority,
        UserResponse assignedUser,
        Long projectId,
        UserResponse createdBy,
        LocalDate dueDate,
        Instant createdAt,
        Instant updatedAt
) {
    public static TaskResponse from(Task task) {
        return new TaskResponse(
                task.getId(),
                task.getTitle(),
                task.getDescription(),
                task.getStatus(),
                task.getPriority(),
                task.getAssignedUser() == null ? null : UserResponse.from(task.getAssignedUser()),
                task.getProject().getId(),
                UserResponse.from(task.getCreatedBy()),
                task.getDueDate(),
                task.getCreatedAt(),
                task.getUpdatedAt()
        );
    }
}
