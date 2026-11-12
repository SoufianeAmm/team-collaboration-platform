package com.teamcollab.backend.service;

import com.teamcollab.backend.dto.AddMemberRequest;
import com.teamcollab.backend.dto.ProjectRequest;
import com.teamcollab.backend.entity.FileUpload;
import com.teamcollab.backend.entity.Notification;
import com.teamcollab.backend.entity.NotificationType;
import com.teamcollab.backend.entity.Project;
import com.teamcollab.backend.entity.ProjectStatus;
import com.teamcollab.backend.entity.Role;
import com.teamcollab.backend.entity.User;
import com.teamcollab.backend.exception.ResourceNotFoundException;
import com.teamcollab.backend.repository.FileUploadRepository;
import com.teamcollab.backend.repository.MessageRepository;
import com.teamcollab.backend.repository.NotificationRepository;
import com.teamcollab.backend.repository.ProjectRepository;
import com.teamcollab.backend.repository.UserRepository;
import com.teamcollab.backend.storage.FileStorageService;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ProjectService {

    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;
    private final ProjectAccessPolicy accessPolicy;
    private final NotificationService notificationService;
    private final MessageRepository messageRepository;
    private final FileUploadRepository fileUploadRepository;
    private final NotificationRepository notificationRepository;
    private final FileStorageService fileStorageService;

    public ProjectService(ProjectRepository projectRepository, UserRepository userRepository,
                           ProjectAccessPolicy accessPolicy, NotificationService notificationService,
                           MessageRepository messageRepository, FileUploadRepository fileUploadRepository,
                           NotificationRepository notificationRepository, FileStorageService fileStorageService) {
        this.projectRepository = projectRepository;
        this.userRepository = userRepository;
        this.accessPolicy = accessPolicy;
        this.notificationService = notificationService;
        this.messageRepository = messageRepository;
        this.fileUploadRepository = fileUploadRepository;
        this.notificationRepository = notificationRepository;
        this.fileStorageService = fileStorageService;
    }

    public Project createProject(ProjectRequest request, User owner) {
        Project project = new Project(request.getName(), request.getDescription(), owner);
        if (request.getStatus() != null) {
            project.setStatus(request.getStatus());
        } else {
            project.setStatus(ProjectStatus.PLANNING);
        }
        return projectRepository.save(project);
    }

    public List<Project> getProjectsForUser(User user) {
        if (user.getRole() == Role.ADMIN) {
            return projectRepository.findAll();
        }
        return projectRepository.findByMembersContaining(user);
    }

    public Project getProjectById(Long id, User requester) {
        Project project = findProjectOrThrow(id);
        accessPolicy.assertMember(project, requester);
        return project;
    }

    public Project updateProject(Long id, ProjectRequest request, User requester) {
        Project project = findProjectOrThrow(id);
        accessPolicy.assertOwnerOrAdmin(project, requester);

        project.setName(request.getName());
        project.setDescription(request.getDescription());
        if (request.getStatus() != null) {
            project.setStatus(request.getStatus());
        }
        return projectRepository.save(project);
    }

    public void deleteProject(Long id, User requester) {
        Project project = findProjectOrThrow(id);
        accessPolicy.assertOwnerOrAdmin(project, requester);
        deleteProjectCascade(project);
    }

    /**
     * The actual delete, minus any access check - exists separately so UserService can reuse it
     * when cascading a deleted user's own (non-active) projects, without needing a "requester."
     *
     * Tasks and their comments cascade automatically via the @OneToMany on Project/Task, but chat
     * messages and file uploads don't have an equivalent mapping, so they're cleaned up explicitly
     * here first - otherwise deleting a project with any chat history or files would fail (or,
     * depending on SQLite's foreign-key enforcement, silently leave orphaned rows behind).
     * Notifications just lose their "jump to" link instead of being deleted outright, so a
     * recipient doesn't lose their notification history just because the project it was about
     * got removed.
     */
    @Transactional
    public void deleteProjectCascade(Project project) {
        for (Notification notification : notificationRepository.findByRelatedProject(project)) {
            notification.setRelatedProject(null);
            notification.setRelatedTask(null);
        }

        messageRepository.deleteAll(messageRepository.findByProjectOrderByCreatedAtAsc(project));

        List<FileUpload> files = fileUploadRepository.findByProject(project);
        for (FileUpload file : files) {
            fileStorageService.delete(file.getFilename());
        }
        fileUploadRepository.deleteAll(files);

        projectRepository.delete(project);
    }

    public List<User> getMembers(Long projectId, User requester) {
        Project project = findProjectOrThrow(projectId);
        accessPolicy.assertMember(project, requester);
        return List.copyOf(project.getMembers());
    }

    public Project addMember(Long projectId, AddMemberRequest request, User requester) {
        Project project = findProjectOrThrow(projectId);
        accessPolicy.assertOwnerOrAdmin(project, requester);

        User newMember = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "No user found with email " + request.getEmail()));

        project.getMembers().add(newMember);
        Project saved = projectRepository.save(project);
        notificationService.notify(newMember, NotificationType.PROJECT_INVITE,
                requester.getFirstName() + " added you to project '" + project.getName() + "'", project, null);
        return saved;
    }

    public Project removeMember(Long projectId, Long userId, User requester) {
        Project project = findProjectOrThrow(projectId);
        accessPolicy.assertOwnerOrAdmin(project, requester);

        if (project.getOwner().getId().equals(userId)) {
            throw new AccessDeniedException("The project owner cannot be removed");
        }

        project.getMembers().removeIf(member -> member.getId().equals(userId));
        return projectRepository.save(project);
    }

    private Project findProjectOrThrow(Long id) {
        return projectRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Project " + id + " not found"));
    }
}
