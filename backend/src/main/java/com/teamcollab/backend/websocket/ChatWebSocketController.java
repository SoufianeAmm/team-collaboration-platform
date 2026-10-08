package com.teamcollab.backend.websocket;

import com.teamcollab.backend.dto.ChatMessageRequest;
import com.teamcollab.backend.dto.MessageResponse;
import com.teamcollab.backend.entity.Message;
import com.teamcollab.backend.service.MessageService;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.security.Principal;

@Controller
public class ChatWebSocketController {

    private final MessageService messageService;
    private final SimpMessagingTemplate messagingTemplate;

    public ChatWebSocketController(MessageService messageService, SimpMessagingTemplate messagingTemplate) {
        this.messageService = messageService;
        this.messagingTemplate = messagingTemplate;
    }

    // A client SENDs to /app/chat.send/{projectId} (the /app prefix is added by the broker config,
    // not written here). We persist it via the same service/authorization path a REST endpoint
    // would use, then broadcast it to everyone currently subscribed to that project's topic.
    @MessageMapping("/chat.send/{projectId}")
    public void send(@DestinationVariable Long projectId, ChatMessageRequest request, Principal principal) {
        var sender = StompAuthChannelInterceptor.resolveUser(principal);
        Message saved = messageService.send(projectId, request.content(), sender);
        messagingTemplate.convertAndSend(
                "/topic/projects/" + projectId + "/chat", MessageResponse.from(saved));
    }
}
