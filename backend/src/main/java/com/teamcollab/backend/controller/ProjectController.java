package com.teamcollab.backend.controller;

import com.teamcollab.backend.dto.AddMemberRequest;
import com.teamcollab.backend.dto.ProjectRequest;
import com.teamcollab.backend.dto.ProjectResponse;
import com.teamcollab.backend.dto.UserResponse;
import com.teamcollab.backend.security.UserPrincipal;
import com.teamcollab.backend.service.ProjectService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/projects")
public class ProjectController {

    private final ProjectService projectService;

    public ProjectController(ProjectService projectService) {
        this.projectService = projectService;
    }

    @GetMapping
    public List<ProjectResponse> list(@AuthenticationPrincipal UserPrincipal principal) {
        return projectService.getProjectsForUser(principal.getUser())
                .stream().map(ProjectResponse::from).toList();
    }

    // Coarse role gate: only these roles can even attempt to create a project.
    // Resource-level checks (ownership, membership) live in the service layer for every other endpoint.
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyRole('PROJECT_MANAGER', 'ADMIN')")
    public ProjectResponse create(@Valid @RequestBody ProjectRequest request,
                                   @AuthenticationPrincipal UserPrincipal principal) {
        return ProjectResponse.from(projectService.createProject(request, principal.getUser()));
    }

    @GetMapping("/{id}")
    public ProjectResponse get(@PathVariable Long id, @AuthenticationPrincipal UserPrincipal principal) {
        return ProjectResponse.from(projectService.getProjectById(id, principal.getUser()));
    }

    @PutMapping("/{id}")
    public ProjectResponse update(@PathVariable Long id, @Valid @RequestBody ProjectRequest request,
                                   @AuthenticationPrincipal UserPrincipal principal) {
        return ProjectResponse.from(projectService.updateProject(id, request, principal.getUser()));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id, @AuthenticationPrincipal UserPrincipal principal) {
        projectService.deleteProject(id, principal.getUser());
    }

    @GetMapping("/{id}/members")
    public List<UserResponse> members(@PathVariable Long id, @AuthenticationPrincipal UserPrincipal principal) {
        return projectService.getMembers(id, principal.getUser())
                .stream().map(UserResponse::from).toList();
    }

    @PostMapping("/{id}/members")
    public List<UserResponse> addMember(@PathVariable Long id, @Valid @RequestBody AddMemberRequest request,
                                         @AuthenticationPrincipal UserPrincipal principal) {
        return projectService.addMember(id, request, principal.getUser())
                .getMembers().stream().map(UserResponse::from).toList();
    }

    @DeleteMapping("/{id}/members/{userId}")
    public List<UserResponse> removeMember(@PathVariable Long id, @PathVariable Long userId,
                                            @AuthenticationPrincipal UserPrincipal principal) {
        return projectService.removeMember(id, userId, principal.getUser())
                .getMembers().stream().map(UserResponse::from).toList();
    }
}
