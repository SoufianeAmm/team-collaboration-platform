package com.teamcollab.backend.controller;

import com.teamcollab.backend.dto.TaskRequest;
import com.teamcollab.backend.dto.TaskResponse;
import com.teamcollab.backend.dto.TaskUpdateRequest;
import com.teamcollab.backend.security.UserPrincipal;
import com.teamcollab.backend.service.TaskService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Task creation/listing is nested under its project (/api/projects/{id}/tasks); once a task
 * exists, it's addressed directly by its own id (/api/tasks/{id}) since a project is implied
 * by the task itself.
 */
@RestController
public class TaskController {

    private final TaskService taskService;

    public TaskController(TaskService taskService) {
        this.taskService = taskService;
    }

    @GetMapping("/api/projects/{projectId}/tasks")
    public List<TaskResponse> listForProject(@PathVariable Long projectId,
                                              @AuthenticationPrincipal UserPrincipal principal) {
        return taskService.getTasksForProject(projectId, principal.getUser())
                .stream().map(TaskResponse::from).toList();
    }

    @PostMapping("/api/projects/{projectId}/tasks")
    @ResponseStatus(HttpStatus.CREATED)
    public TaskResponse create(@PathVariable Long projectId, @Valid @RequestBody TaskRequest request,
                                @AuthenticationPrincipal UserPrincipal principal) {
        return TaskResponse.from(taskService.createTask(projectId, request, principal.getUser()));
    }

    @GetMapping("/api/tasks/{id}")
    public TaskResponse get(@PathVariable Long id, @AuthenticationPrincipal UserPrincipal principal) {
        return TaskResponse.from(taskService.getTaskById(id, principal.getUser()));
    }

    @PutMapping("/api/tasks/{id}")
    public TaskResponse update(@PathVariable Long id, @Valid @RequestBody TaskUpdateRequest request,
                                @AuthenticationPrincipal UserPrincipal principal) {
        return TaskResponse.from(taskService.updateTask(id, request, principal.getUser()));
    }

    @DeleteMapping("/api/tasks/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id, @AuthenticationPrincipal UserPrincipal principal) {
        taskService.deleteTask(id, principal.getUser());
    }
}
