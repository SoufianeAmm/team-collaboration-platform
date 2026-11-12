package com.teamcollab.backend.service;

import com.teamcollab.backend.dto.UpdateProfileRequest;
import com.teamcollab.backend.dto.UpdateUserRequest;
import com.teamcollab.backend.entity.FileUpload;
import com.teamcollab.backend.entity.Project;
import com.teamcollab.backend.entity.ProjectStatus;
import com.teamcollab.backend.entity.Role;
import com.teamcollab.backend.entity.Task;
import com.teamcollab.backend.entity.User;
import com.teamcollab.backend.exception.EmailAlreadyExistsException;
import com.teamcollab.backend.exception.FileTooLargeException;
import com.teamcollab.backend.exception.InvalidFileTypeException;
import com.teamcollab.backend.exception.InvalidRoleChangeException;
import com.teamcollab.backend.exception.ResourceNotFoundException;
import com.teamcollab.backend.exception.UserHasActiveProjectsException;
import com.teamcollab.backend.repository.CommentRepository;
import com.teamcollab.backend.repository.FileUploadRepository;
import com.teamcollab.backend.repository.MessageRepository;
import com.teamcollab.backend.repository.NotificationRepository;
import com.teamcollab.backend.repository.ProjectRepository;
import com.teamcollab.backend.repository.TaskRepository;
import com.teamcollab.backend.repository.UserRepository;
import com.teamcollab.backend.storage.FileStorageService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Locale;
import java.util.Set;

@Service
public class UserService {

    private static final Set<String> ALLOWED_AVATAR_EXTENSIONS = Set.of("png", "jpg", "jpeg", "gif", "webp");

    private final UserRepository userRepository;
    private final FileStorageService fileStorageService;
    private final PasswordEncoder passwordEncoder;
    private final ProjectRepository projectRepository;
    private final ProjectService projectService;
    private final TaskRepository taskRepository;
    private final CommentRepository commentRepository;
    private final MessageRepository messageRepository;
    private final NotificationRepository notificationRepository;
    private final FileUploadRepository fileUploadRepository;
    private final long maxAvatarSizeBytes;

    public UserService(UserRepository userRepository, FileStorageService fileStorageService,
                        PasswordEncoder passwordEncoder, ProjectRepository projectRepository,
                        ProjectService projectService, TaskRepository taskRepository,
                        CommentRepository commentRepository, MessageRepository messageRepository,
                        NotificationRepository notificationRepository, FileUploadRepository fileUploadRepository,
                        @Value("${file.max-size-mb}") long maxSizeMb) {
        this.userRepository = userRepository;
        this.fileStorageService = fileStorageService;
        this.passwordEncoder = passwordEncoder;
        this.projectRepository = projectRepository;
        this.projectService = projectService;
        this.taskRepository = taskRepository;
        this.commentRepository = commentRepository;
        this.messageRepository = messageRepository;
        this.notificationRepository = notificationRepository;
        this.fileUploadRepository = fileUploadRepository;
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

    /**
     * Admin-only: edits another user's name/email, and optionally resets their password - the
     * "they forgot their password" recovery path, since there's no self-service reset flow.
     */
    public User updateUser(Long id, UpdateUserRequest request, User requester) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User " + id + " not found"));

        if (!user.getEmail().equalsIgnoreCase(request.getEmail())
                && userRepository.existsByEmail(request.getEmail())) {
            throw new EmailAlreadyExistsException(request.getEmail());
        }

        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setEmail(request.getEmail());
        if (request.getPassword() != null && !request.getPassword().isBlank()) {
            user.setPassword(passwordEncoder.encode(request.getPassword()));
        }
        return userRepository.save(user);
    }

    /**
     * Admin-only delete. Blocked entirely if the user owns a project that's still PLANNING or
     * ACTIVE - deleting them would leave that project without an owner. A manager whose owned
     * projects are all COMPLETED/ARCHIVED can still be deleted; those finished projects (and
     * everything in them) get cleaned up along with the account. Everything else the user is
     * attached to elsewhere (tasks assigned to them, comments, chat messages, uploaded files,
     * notifications, other projects they're just a member of) is cleaned up or unlinked so
     * nothing is left pointing at a user that no longer exists.
     */
    @Transactional
    public void deleteUser(Long id, User requester) {
        if (id.equals(requester.getId())) {
            throw new InvalidRoleChangeException("You cannot delete your own account");
        }
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User " + id + " not found"));

        List<Project> ownedProjects = projectRepository.findByOwner(user);
        boolean hasActiveProject = ownedProjects.stream()
                .anyMatch(p -> p.getStatus() == ProjectStatus.PLANNING || p.getStatus() == ProjectStatus.ACTIVE);
        if (hasActiveProject) {
            throw new UserHasActiveProjectsException(
                    "Cannot delete " + user.getFirstName() + " " + user.getLastName()
                            + " - they still own an active project. Transfer ownership or complete/archive it first.");
        }
        for (Project project : ownedProjects) {
            projectService.deleteProjectCascade(project);
        }

        for (Project project : projectRepository.findByMembersContaining(user)) {
            project.getMembers().remove(user);
            projectRepository.save(project);
        }

        List<Task> assignedTasks = taskRepository.findByAssignedUser(user);
        assignedTasks.forEach(task -> task.setAssignedUser(null));
        taskRepository.saveAll(assignedTasks);

        List<Task> createdTasks = taskRepository.findByCreatedBy(user);
        createdTasks.forEach(task -> task.setCreatedBy(null));
        taskRepository.saveAll(createdTasks);

        commentRepository.deleteAll(commentRepository.findByAuthor(user));
        messageRepository.deleteAll(messageRepository.findBySender(user));
        notificationRepository.deleteAll(notificationRepository.findByRecipientOrderByCreatedAtDesc(user));

        List<FileUpload> uploads = fileUploadRepository.findByUploader(user);
        for (FileUpload upload : uploads) {
            fileStorageService.delete(upload.getFilename());
        }
        fileUploadRepository.deleteAll(uploads);

        userRepository.delete(user);
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
