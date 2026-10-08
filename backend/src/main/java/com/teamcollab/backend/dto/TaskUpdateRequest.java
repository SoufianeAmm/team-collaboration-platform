package com.teamcollab.backend.dto;

import com.teamcollab.backend.entity.TaskPriority;
import com.teamcollab.backend.entity.TaskStatus;
import jakarta.validation.constraints.NotBlank;

import java.time.LocalDate;

/**
 * Full replace, not a partial patch - the frontend is expected to send the complete task
 * on every edit (including drag-and-drop status changes on the Kanban board).
 */
public class TaskUpdateRequest {

    @NotBlank
    private String title;

    private String description;

    private TaskStatus status;

    private TaskPriority priority;

    private Long assignedUserId;

    private LocalDate dueDate;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public TaskStatus getStatus() {
        return status;
    }

    public void setStatus(TaskStatus status) {
        this.status = status;
    }

    public TaskPriority getPriority() {
        return priority;
    }

    public void setPriority(TaskPriority priority) {
        this.priority = priority;
    }

    public Long getAssignedUserId() {
        return assignedUserId;
    }

    public void setAssignedUserId(Long assignedUserId) {
        this.assignedUserId = assignedUserId;
    }

    public LocalDate getDueDate() {
        return dueDate;
    }

    public void setDueDate(LocalDate dueDate) {
        this.dueDate = dueDate;
    }
}
