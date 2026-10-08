package com.teamcollab.backend.service;

import com.teamcollab.backend.dto.NotificationResponse;
import com.teamcollab.backend.entity.Notification;
import com.teamcollab.backend.entity.NotificationType;
import com.teamcollab.backend.entity.Project;
import com.teamcollab.backend.entity.Task;
import com.teamcollab.backend.entity.User;
import com.teamcollab.backend.exception.ResourceNotFoundException;
import com.teamcollab.backend.repository.NotificationRepository;
import com.teamcollab.backend.repository.ProjectRepository;
import com.teamcollab.backend.websocket.NotificationPublisher;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
public class NotificationService {

    private static final Pattern MENTION_PATTERN = Pattern.compile("@(\\w+)");

    private final NotificationRepository notificationRepository;
    private final ProjectRepository projectRepository;
    private final NotificationPublisher publisher;

    public NotificationService(NotificationRepository notificationRepository, ProjectRepository projectRepository,
                                NotificationPublisher publisher) {
        this.notificationRepository = notificationRepository;
        this.projectRepository = projectRepository;
        this.publisher = publisher;
    }

    public List<Notification> listForUser(User user) {
        return notificationRepository.findByRecipientOrderByCreatedAtDesc(user);
    }

    public Notification markAsRead(Long id, User requester) {
        Notification notification = notificationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Notification " + id + " not found"));
        if (!notification.getRecipient().getId().equals(requester.getId())) {
            throw new AccessDeniedException("You can only mark your own notifications as read");
        }
        notification.markAsRead();
        return notificationRepository.save(notification);
    }

    /** Creates, persists, and pushes a notification live over WebSocket - used by every trigger below. */
    public void notify(User recipient, NotificationType type, String content, Project relatedProject, Task relatedTask) {
        Notification saved = notificationRepository.save(
                new Notification(recipient, type, content, relatedProject, relatedTask));
        publisher.publish(recipient, NotificationResponse.from(saved));
    }

    /**
     * Scans {@code content} for "@username" mentions, resolves them against the project's actual
     * members (so you can't be "mentioned" by someone typing an arbitrary email/username that
     * isn't even in the project), and notifies each one except the author themselves.
     */
    public void notifyMentions(String content, Project project, User author, String contextLabel) {
        Matcher matcher = MENTION_PATTERN.matcher(content);
        Set<String> mentionedUsernames = matcher.results()
                .map(m -> m.group(1))
                .collect(Collectors.toSet());
        if (mentionedUsernames.isEmpty()) {
            return;
        }

        List<User> members = projectRepository.findMembers(project.getId());
        for (User member : members) {
            if (mentionedUsernames.contains(member.getUsername()) && !member.getId().equals(author.getId())) {
                notify(member, NotificationType.MENTION,
                        author.getFirstName() + " mentioned you in " + contextLabel, project, null);
            }
        }
    }
}
