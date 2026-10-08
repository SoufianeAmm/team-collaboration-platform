package com.teamcollab.backend.dto;

public record AuthResponse(String token, UserResponse user) {
}
