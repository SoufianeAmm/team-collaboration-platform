package com.teamcollab.backend.websocket;

import com.teamcollab.backend.dto.TaskEvent;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

/**
 * Keeps the "push a WS event" concern out of TaskService's business logic - the service just
 * calls one of these methods and doesn't need to know or care about topic naming/SimpMessagingTemplate.
 */
@Component
public class TaskEventPublisher {

    private final SimpMessagingTemplate messagingTemplate;

    public TaskEventPublisher(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    public void publish(Long projectId, TaskEvent event) {
        messagingTemplate.convertAndSend("/topic/projects/" + projectId + "/tasks", event);
    }
}
