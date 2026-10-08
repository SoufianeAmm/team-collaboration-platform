package com.teamcollab.backend.controller;

import com.teamcollab.backend.dto.NotificationResponse;
import com.teamcollab.backend.security.UserPrincipal;
import com.teamcollab.backend.service.NotificationService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @GetMapping
    public List<NotificationResponse> list(@AuthenticationPrincipal UserPrincipal principal) {
        return notificationService.listForUser(principal.getUser())
                .stream().map(NotificationResponse::from).toList();
    }

    @PutMapping("/{id}/read")
    public NotificationResponse markAsRead(@PathVariable Long id, @AuthenticationPrincipal UserPrincipal principal) {
        return NotificationResponse.from(notificationService.markAsRead(id, principal.getUser()));
    }
}
