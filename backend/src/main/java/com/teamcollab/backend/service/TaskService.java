package com.teamcollab.backend.service;

import com.teamcollab.backend.dto.TaskEvent;
import com.teamcollab.backend.dto.TaskRequest;
import com.teamcollab.backend.dto.TaskResponse;
import com.teamcollab.backend.dto.TaskUpdateRequest;
import com.teamcollab.backend.entity.NotificationType;
import com.teamcollab.backend.entity.Project;
import com.teamcollab.backend.entity.Task;
import com.teamcollab.backend.entity.User;
import com.teamcollab.backend.exception.ResourceNotFoundException;
import com.teamcollab.backend.repository.ProjectRepository;
import com.teamcollab.backend.repository.TaskRepository;
import com.teamcollab.backend.repository.UserRepository;
import com.teamcollab.backend.websocket.TaskEventPublisher;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

@Service
public class TaskService {

    private final TaskRepository taskRepository;
    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;
    private final ProjectAccessPolicy accessPolicy;
    private final TaskEventPublisher eventPublisher;
    private final NotificationService notificationService;

    public TaskService(TaskRepository taskRepository, ProjectRepository projectRepository,
                        UserRepository userRepository, ProjectAccessPolicy accessPolicy,
                        TaskEventPublisher eventPublisher, NotificationService notificationService) {
        this.taskRepository = taskRepository;
        this.projectRepository = projectRepository;
        this.userRepository = userRepository;
        this.accessPolicy = accessPolicy;
        this.eventPublisher = eventPublisher;
        this.notificationService = notificationService;
    }

    public Task createTask(Long projectId, TaskRequest request, User creator) {
        Project project = findProjectOrThrow(projectId);
        accessPolicy.assertMember(project, creator);

        Task task = new Task(request.getTitle(), request.getDescription(), project, creator);
        if (request.getPriority() != null) {
            task.setPriority(request.getPriority());
        }
        task.setDueDate(request.getDueDate());

        if (request.getAssignedUserId() != null) {
            task.setAssignedUser(resolveProjectMember(project, request.getAssignedUserId()));
        }

        Task saved = taskRepository.save(task);
        eventPublisher.publish(projectId, TaskEvent.created(TaskResponse.from(saved)));

        User assignee = saved.getAssignedUser();
        if (assignee != null && !assignee.getId().equals(creator.getId())) {
            notificationService.notify(assignee, NotificationType.TASK_ASSIGNED,
                    creator.getFirstName() + " assigned you to task '" + saved.getTitle() + "'", project, saved);
        }

        return saved;
    }

    public List<Task> getTasksForProject(Long projectId, User requester) {
        Project project = findProjectOrThrow(projectId);
        accessPolicy.assertMember(project, requester);
        return taskRepository.findByProject(project);
    }

    public Task getTaskById(Long id, User requester) {
        Task task = findTaskOrThrow(id);
        accessPolicy.assertMember(task.getProject(), requester);
        return task;
    }

    public Task updateTask(Long id, TaskUpdateRequest request, User requester) {
        Task task = findTaskOrThrow(id);
        Project project = task.getProject();

        boolean isAssignee = task.getAssignedUser() != null
                && task.getAssignedUser().getId().equals(requester.getId());
        if (!accessPolicy.isOwnerOrAdmin(project, requester) && !isAssignee) {
            throw new AccessDeniedException(
                    "Only the project owner, an admin, or the assigned user can update this task");
        }

        User previousAssignee = task.getAssignedUser();
        var previousStatus = task.getStatus();

        task.setTitle(request.getTitle());
        task.setDescription(request.getDescription());
        task.setDueDate(request.getDueDate());
        if (request.getStatus() != null) {
            task.setStatus(request.getStatus());
        }
        if (request.getPriority() != null) {
            task.setPriority(request.getPriority());
        }
        if (request.getAssignedUserId() == null) {
            task.setAssignedUser(null);
        } else {
            task.setAssignedUser(resolveProjectMember(project, request.getAssignedUserId()));
        }

        Task saved = taskRepository.save(task);
        eventPublisher.publish(project.getId(), TaskEvent.updated(TaskResponse.from(saved)));

        User newAssignee = saved.getAssignedUser();
        boolean assigneeChanged = !Objects.equals(
                previousAssignee == null ? null : previousAssignee.getId(),
                newAssignee == null ? null : newAssignee.getId());

        if (assigneeChanged && newAssignee != null && !newAssignee.getId().equals(requester.getId())) {
            notificationService.notify(newAssignee, NotificationType.TASK_ASSIGNED,
                    requester.getFirstName() + " assigned you to task '" + saved.getTitle() + "'", project, saved);
        } else if (!assigneeChanged && newAssignee != null && saved.getStatus() != previousStatus
                && !newAssignee.getId().equals(requester.getId())) {
            // Only fire the "status changed" notification when someone OTHER than the assignee
            // made the change - the assignee doesn't need to be told about their own edit.
            notificationService.notify(newAssignee, NotificationType.TASK_STATUS_CHANGED,
                    "Task '" + saved.getTitle() + "' status changed to " + saved.getStatus(), project, saved);
        }

        return saved;
    }

    public void deleteTask(Long id, User requester) {
        Task task = findTaskOrThrow(id);
        accessPolicy.assertOwnerOrAdmin(task.getProject(), requester);
        Long projectId = task.getProject().getId();
        taskRepository.delete(task);
        eventPublisher.publish(projectId, TaskEvent.deleted(id));
    }

    private User resolveProjectMember(Project project, Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User " + userId + " not found"));
        accessPolicy.assertMember(project, user);
        return user;
    }

    private Project findProjectOrThrow(Long id) {
        return projectRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Project " + id + " not found"));
    }

    private Task findTaskOrThrow(Long id) {
        return taskRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Task " + id + " not found"));
    }
}
