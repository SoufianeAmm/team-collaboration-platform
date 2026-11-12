package com.teamcollab.backend.exception;

public class UserHasActiveProjectsException extends RuntimeException {
    public UserHasActiveProjectsException(String message) {
        super(message);
    }
}
