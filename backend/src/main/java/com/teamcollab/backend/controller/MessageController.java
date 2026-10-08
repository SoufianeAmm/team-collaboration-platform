package com.teamcollab.backend.controller;

import com.teamcollab.backend.dto.MessageResponse;
import com.teamcollab.backend.security.UserPrincipal;
import com.teamcollab.backend.service.MessageService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * REST handles history (a normal GET, cacheable, paginatable later); new messages are
 * delivered live over the WebSocket/STOMP channel in the websocket package, not here.
 */
@RestController
public class MessageController {

    private final MessageService messageService;

    public MessageController(MessageService messageService) {
        this.messageService = messageService;
    }

    @GetMapping("/api/projects/{projectId}/messages")
    public List<MessageResponse> history(@PathVariable Long projectId, @AuthenticationPrincipal UserPrincipal principal) {
        return messageService.getHistory(projectId, principal.getUser())
                .stream().map(MessageResponse::from).toList();
    }
}
