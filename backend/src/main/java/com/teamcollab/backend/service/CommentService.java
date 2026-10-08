package com.teamcollab.backend.service;

import com.teamcollab.backend.dto.CommentRequest;
import com.teamcollab.backend.entity.Comment;
import com.teamcollab.backend.entity.NotificationType;
import com.teamcollab.backend.entity.Task;
import com.teamcollab.backend.entity.User;
import com.teamcollab.backend.exception.ResourceNotFoundException;
import com.teamcollab.backend.repository.CommentRepository;
import com.teamcollab.backend.repository.TaskRepository;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CommentService {

    private final CommentRepository commentRepository;
    private final TaskRepository taskRepository;
    private final ProjectAccessPolicy accessPolicy;
    private final NotificationService notificationService;

    public CommentService(CommentRepository commentRepository, TaskRepository taskRepository,
                           ProjectAccessPolicy accessPolicy, NotificationService notificationService) {
        this.commentRepository = commentRepository;
        this.taskRepository = taskRepository;
        this.accessPolicy = accessPolicy;
        this.notificationService = notificationService;
    }

    public List<Comment> listForTask(Long taskId, User requester) {
        Task task = findTaskOrThrow(taskId);
        accessPolicy.assertMember(task.getProject(), requester);
        return commentRepository.findByTask(task);
    }

    public Comment create(Long taskId, CommentRequest request, User author) {
        Task task = findTaskOrThrow(taskId);
        accessPolicy.assertMember(task.getProject(), author);
        Comment saved = commentRepository.save(new Comment(request.getContent(), author, task));

        User assignee = task.getAssignedUser();
        if (assignee != null && !assignee.getId().equals(author.getId())) {
            notificationService.notify(assignee, NotificationType.COMMENT_ADDED,
                    author.getFirstName() + " commented on task '" + task.getTitle() + "'",
                    task.getProject(), task);
        }
        notificationService.notifyMentions(request.getContent(), task.getProject(), author,
                "a comment on task '" + task.getTitle() + "'");

        return saved;
    }

    public Comment update(Long commentId, CommentRequest request, User requester) {
        Comment comment = findCommentOrThrow(commentId);
        if (!comment.getAuthor().getId().equals(requester.getId())) {
            throw new AccessDeniedException("Only the comment's author can edit it");
        }
        comment.setContent(request.getContent());
        return commentRepository.save(comment);
    }

    public void delete(Long commentId, User requester) {
        Comment comment = findCommentOrThrow(commentId);
        boolean isAuthor = comment.getAuthor().getId().equals(requester.getId());
        if (!isAuthor && !accessPolicy.isOwnerOrAdmin(comment.getTask().getProject(), requester)) {
            throw new AccessDeniedException("Only the author, the project owner, or an admin can delete this comment");
        }
        commentRepository.delete(comment);
    }

    private Task findTaskOrThrow(Long id) {
        return taskRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Task " + id + " not found"));
    }

    private Comment findCommentOrThrow(Long id) {
        return commentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Comment " + id + " not found"));
    }
}
