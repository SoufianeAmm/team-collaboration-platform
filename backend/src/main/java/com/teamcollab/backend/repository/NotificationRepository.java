package com.teamcollab.backend.repository;

import com.teamcollab.backend.entity.Notification;
import com.teamcollab.backend.entity.NotificationType;
import com.teamcollab.backend.entity.Project;
import com.teamcollab.backend.entity.Task;
import com.teamcollab.backend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Long> {
    List<Notification> findByRecipientOrderByCreatedAtDesc(User recipient);
    List<Notification> findByRelatedProject(Project project);

    // Used by the deadline-approaching scheduled job so it only ever notifies once per task,
    // instead of re-notifying every day the task remains due-soon-and-incomplete.
    boolean existsByRelatedTaskAndType(Task task, NotificationType type);
}
