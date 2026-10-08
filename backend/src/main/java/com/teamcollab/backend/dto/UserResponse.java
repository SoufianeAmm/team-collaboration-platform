package com.teamcollab.backend.dto;

import com.teamcollab.backend.entity.Role;
import com.teamcollab.backend.entity.User;

import java.time.Instant;

/**
 * Safe, public view of a User - deliberately excludes the password hash.
 * Entities are never returned directly from controllers; this is what goes over the wire instead.
 */
public record UserResponse(
        Long id,
        String username,
        String firstName,
        String lastName,
        String email,
        Role role,
        String profilePictureUrl,
        Instant createdAt
) {
    public static UserResponse from(User user) {
        return new UserResponse(
                user.getId(),
                user.getUsername(),
                user.getFirstName(),
                user.getLastName(),
                user.getEmail(),
                user.getRole(),
                user.getProfilePictureUrl(),
                user.getCreatedAt()
        );
    }
}
