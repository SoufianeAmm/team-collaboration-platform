package com.teamcollab.backend.service;

import com.teamcollab.backend.dto.UpdateProfileRequest;
import com.teamcollab.backend.entity.Role;
import com.teamcollab.backend.entity.User;
import com.teamcollab.backend.exception.FileTooLargeException;
import com.teamcollab.backend.exception.InvalidFileTypeException;
import com.teamcollab.backend.exception.InvalidRoleChangeException;
import com.teamcollab.backend.exception.ResourceNotFoundException;
import com.teamcollab.backend.repository.UserRepository;
import com.teamcollab.backend.storage.FileStorageService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Locale;
import java.util.Set;

@Service
public class UserService {

    private static final Set<String> ALLOWED_AVATAR_EXTENSIONS = Set.of("png", "jpg", "jpeg", "gif", "webp");

    private final UserRepository userRepository;
    private final FileStorageService fileStorageService;
    private final long maxAvatarSizeBytes;

    public UserService(UserRepository userRepository, FileStorageService fileStorageService,
                        @Value("${file.max-size-mb}") long maxSizeMb) {
        this.userRepository = userRepository;
        this.fileStorageService = fileStorageService;
        this.maxAvatarSizeBytes = maxSizeMb * 1024 * 1024;
    }

    public List<User> listAll() {
        return userRepository.findAll();
    }

    /** Admin-only role change. Self-changes are blocked so an admin can never accidentally lock themselves out. */
    public User updateRole(Long id, Role newRole, User requester) {
        if (id.equals(requester.getId())) {
            throw new InvalidRoleChangeException("You cannot change your own role");
        }
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User " + id + " not found"));
        user.setRole(newRole);
        return userRepository.save(user);
    }

    public User updateProfile(User requester, UpdateProfileRequest request) {
        requester.setFirstName(request.getFirstName());
        requester.setLastName(request.getLastName());
        return userRepository.save(requester);
    }

    public User updateAvatar(User requester, MultipartFile file) {
        if (file.isEmpty()) {
            throw new InvalidFileTypeException("Cannot upload an empty file");
        }
        String extension = extensionOf(file.getOriginalFilename()).toLowerCase(Locale.ROOT);
        if (!ALLOWED_AVATAR_EXTENSIONS.contains(extension)) {
            throw new InvalidFileTypeException(
                    "File type '." + extension + "' is not allowed. Allowed types: " + ALLOWED_AVATAR_EXTENSIONS);
        }
        if (file.getSize() > maxAvatarSizeBytes) {
            throw new FileTooLargeException(
                    "File exceeds the maximum allowed size of " + (maxAvatarSizeBytes / 1024 / 1024) + " MB");
        }

        deleteStoredAvatarIfAny(requester);
        String storedFilename = fileStorageService.store(file);
        requester.setProfilePictureUrl("/api/avatars/" + storedFilename);
        return userRepository.save(requester);
    }

    public User removeAvatar(User requester) {
        deleteStoredAvatarIfAny(requester);
        requester.setProfilePictureUrl(null);
        return userRepository.save(requester);
    }

    private void deleteStoredAvatarIfAny(User user) {
        String existingUrl = user.getProfilePictureUrl();
        if (existingUrl != null && existingUrl.startsWith("/api/avatars/")) {
            fileStorageService.delete(existingUrl.substring("/api/avatars/".length()));
        }
    }

    private String extensionOf(String filename) {
        if (filename == null) return "";
        int dot = filename.lastIndexOf('.');
        return dot == -1 ? "" : filename.substring(dot + 1);
    }
}
