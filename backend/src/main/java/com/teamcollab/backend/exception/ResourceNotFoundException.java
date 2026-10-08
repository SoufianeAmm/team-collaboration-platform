package com.teamcollab.backend.exception;

/**
 * Thrown when a requested entity (project, task, user, ...) doesn't exist.
 * Used from Phase 5 onward wherever a controller looks something up by id.
 */
public class ResourceNotFoundException extends RuntimeException {
    public ResourceNotFoundException(String message) {
        super(message);
    }
}
