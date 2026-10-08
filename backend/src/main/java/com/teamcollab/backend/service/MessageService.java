package com.teamcollab.backend.service;

import com.teamcollab.backend.entity.Message;
import com.teamcollab.backend.entity.Project;
import com.teamcollab.backend.entity.User;
import com.teamcollab.backend.exception.ResourceNotFoundException;
import com.teamcollab.backend.repository.MessageRepository;
import com.teamcollab.backend.repository.ProjectRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MessageService {

    private final MessageRepository messageRepository;
    private final ProjectRepository projectRepository;
    private final ProjectAccessPolicy accessPolicy;
    private final NotificationService notificationService;

    public MessageService(MessageRepository messageRepository, ProjectRepository projectRepository,
                           ProjectAccessPolicy accessPolicy, NotificationService notificationService) {
        this.messageRepository = messageRepository;
        this.projectRepository = projectRepository;
        this.accessPolicy = accessPolicy;
        this.notificationService = notificationService;
    }

    public List<Message> getHistory(Long projectId, User requester) {
        Project project = findProjectOrThrow(projectId);
        accessPolicy.assertMember(project, requester);
        return messageRepository.findByProjectOrderByCreatedAtAsc(project);
    }

    public Message send(Long projectId, String content, User sender) {
        Project project = findProjectOrThrow(projectId);
        accessPolicy.assertMember(project, sender);
        Message saved = messageRepository.save(new Message(content, sender, project));
        // Runs on the WebSocket message thread, not a normal HTTP request - notifyMentions()
        // is written to never touch a lazy collection for exactly that reason.
        notificationService.notifyMentions(content, project, sender, "the " + project.getName() + " chat");
        return saved;
    }

    private Project findProjectOrThrow(Long id) {
        return projectRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Project " + id + " not found"));
    }
}
