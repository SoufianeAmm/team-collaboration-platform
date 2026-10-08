package com.teamcollab.backend.service;

import com.teamcollab.backend.dto.AddMemberRequest;
import com.teamcollab.backend.dto.ProjectRequest;
import com.teamcollab.backend.entity.NotificationType;
import com.teamcollab.backend.entity.Project;
import com.teamcollab.backend.entity.ProjectStatus;
import com.teamcollab.backend.entity.Role;
import com.teamcollab.backend.entity.User;
import com.teamcollab.backend.exception.ResourceNotFoundException;
import com.teamcollab.backend.repository.ProjectRepository;
import com.teamcollab.backend.repository.UserRepository;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProjectService {

    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;
    private final ProjectAccessPolicy accessPolicy;
    private final NotificationService notificationService;

    public ProjectService(ProjectRepository projectRepository, UserRepository userRepository,
                           ProjectAccessPolicy accessPolicy, NotificationService notificationService) {
        this.projectRepository = projectRepository;
        this.userRepository = userRepository;
        this.accessPolicy = accessPolicy;
        this.notificationService = notificationService;
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
