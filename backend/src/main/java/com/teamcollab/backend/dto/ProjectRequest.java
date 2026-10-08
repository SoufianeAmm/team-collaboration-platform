package com.teamcollab.backend.dto;

import com.teamcollab.backend.entity.ProjectStatus;
import jakarta.validation.constraints.NotBlank;

/**
 * Used for both create and update. On create, a null status defaults to PLANNING.
 * On update, a null status leaves the existing status unchanged.
 */
public class ProjectRequest {

    @NotBlank
    private String name;

    private String description;

    private ProjectStatus status;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public ProjectStatus getStatus() {
        return status;
    }

    public void setStatus(ProjectStatus status) {
        this.status = status;
    }
}
