package com.teamcollab.backend.service;

import com.teamcollab.backend.entity.Project;
import com.teamcollab.backend.entity.Role;
import com.teamcollab.backend.entity.User;
import com.teamcollab.backend.repository.ProjectRepository;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Component;

/**
 * Resource-level authorization that @PreAuthorize can't express on its own, since "is this user
 * a member of THIS project" depends on data, not just their role. @PreAuthorize still handles the
 * coarse "does this role even get to call this endpoint at all" checks on top of this.
 */
@Component
public class ProjectAccessPolicy {

    private final ProjectRepository projectRepository;

    public ProjectAccessPolicy(ProjectRepository projectRepository) {
        this.projectRepository = projectRepository;
    }

    public boolean isMember(Project project, User user) {
        // Goes straight to the DB rather than iterating project.getMembers() - that's a lazy
        // collection, and callers invoked outside a normal HTTP request (e.g. WebSocket message
        // handling, which isn't wrapped by Spring's Open-In-View filter) would hit a
        // LazyInitializationException touching it without an active Hibernate session.
        return user.getRole() == Role.ADMIN || projectRepository.isMember(project.getId(), user.getId());
    }

    public boolean isOwnerOrAdmin(Project project, User user) {
        return user.getRole() == Role.ADMIN || project.getOwner().getId().equals(user.getId());
    }

    public void assertMember(Project project, User user) {
        if (!isMember(project, user)) {
            throw new AccessDeniedException("You are not a member of this project");
        }
    }

    public void assertOwnerOrAdmin(Project project, User user) {
        if (!isOwnerOrAdmin(project, user)) {
            throw new AccessDeniedException("Only the project owner or an admin can do this");
        }
    }
}
