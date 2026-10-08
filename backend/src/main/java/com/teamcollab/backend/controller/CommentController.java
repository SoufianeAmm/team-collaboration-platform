package com.teamcollab.backend.controller;

import com.teamcollab.backend.dto.CommentRequest;
import com.teamcollab.backend.dto.CommentResponse;
import com.teamcollab.backend.security.UserPrincipal;
import com.teamcollab.backend.service.CommentService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class CommentController {

    private final CommentService commentService;

    public CommentController(CommentService commentService) {
        this.commentService = commentService;
    }

    @GetMapping("/api/tasks/{taskId}/comments")
    public List<CommentResponse> list(@PathVariable Long taskId, @AuthenticationPrincipal UserPrincipal principal) {
        return commentService.listForTask(taskId, principal.getUser())
                .stream().map(CommentResponse::from).toList();
    }

    @PostMapping("/api/tasks/{taskId}/comments")
    @ResponseStatus(HttpStatus.CREATED)
    public CommentResponse create(@PathVariable Long taskId, @Valid @RequestBody CommentRequest request,
                                   @AuthenticationPrincipal UserPrincipal principal) {
        return CommentResponse.from(commentService.create(taskId, request, principal.getUser()));
    }

    @PutMapping("/api/comments/{id}")
    public CommentResponse update(@PathVariable Long id, @Valid @RequestBody CommentRequest request,
                                   @AuthenticationPrincipal UserPrincipal principal) {
        return CommentResponse.from(commentService.update(id, request, principal.getUser()));
    }

    @DeleteMapping("/api/comments/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id, @AuthenticationPrincipal UserPrincipal principal) {
        commentService.delete(id, principal.getUser());
    }
}
