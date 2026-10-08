package com.teamcollab.backend.websocket;

import com.teamcollab.backend.dto.NotificationResponse;
import com.teamcollab.backend.entity.User;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

/**
 * Unlike chat/task events (broadcast to everyone subscribed to a project topic), a notification
 * belongs to exactly one person. convertAndSendToUser() uses Spring's built-in per-user routing -
 * the client subscribes to "/user/queue/notifications" and only that user's own session(s)
 * ever receive what's sent here, with no project-membership check needed (there's nothing to
 * check: the routing itself is already scoped to the one recipient).
 */
@Component
public class NotificationPublisher {

    private final SimpMessagingTemplate messagingTemplate;

    public NotificationPublisher(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    public void publish(User recipient, NotificationResponse notification) {
        messagingTemplate.convertAndSendToUser(recipient.getEmail(), "/queue/notifications", notification);
    }
}
