package com.teamcollab.backend.exception;

import java.time.Instant;
import java.util.Map;

/**
 * The shape every error from this API comes back as - consistent JSON the frontend can rely on
 * instead of guessing per-endpoint. "fieldErrors" is only populated for validation failures.
 */
public record ErrorResponse(
        Instant timestamp,
        int status,
        String error,
        String message,
        Map<String, String> fieldErrors
) {
    public ErrorResponse(int status, String error, String message) {
        this(Instant.now(), status, error, message, null);
    }

    public ErrorResponse(int status, String error, String message, Map<String, String> fieldErrors) {
        this(Instant.now(), status, error, message, fieldErrors);
    }
}
