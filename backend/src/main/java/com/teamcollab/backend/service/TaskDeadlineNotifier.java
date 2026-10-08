package com.teamcollab.backend.service;

import com.teamcollab.backend.entity.NotificationType;
import com.teamcollab.backend.entity.Task;
import com.teamcollab.backend.entity.TaskStatus;
import com.teamcollab.backend.repository.NotificationRepository;
import com.teamcollab.backend.repository.TaskRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;

/**
 * Runs on a schedule (not triggered by any request) to find tasks due tomorrow and notify their
 * assignee - the one notification trigger that isn't a direct side effect of a user action.
 * Guards against re-notifying the same task every day it stays due-soon-and-incomplete.
 */
@Component
public class TaskDeadlineNotifier {

    private final TaskRepository taskRepository;
    private final NotificationRepository notificationRepository;
    private final NotificationService notificationService;

    public TaskDeadlineNotifier(TaskRepository taskRepository, NotificationRepository notificationRepository,
                                 NotificationService notificationService) {
        this.taskRepository = taskRepository;
        this.notificationRepository = notificationRepository;
        this.notificationService = notificationService;
    }

    @Scheduled(cron = "${app.deadline-check-cron}")
    public void notifyApproachingDeadlines() {
        LocalDate tomorrow = LocalDate.now().plusDays(1);
        List<Task> dueTomorrow = taskRepository.findByDueDateAndStatusNot(tomorrow, TaskStatus.COMPLETED);

        for (Task task : dueTomorrow) {
            if (task.getAssignedUser() == null) {
                continue;
            }
            boolean alreadyNotified = notificationRepository
                    .existsByRelatedTaskAndType(task, NotificationType.DEADLINE_APPROACHING);
            if (alreadyNotified) {
                continue;
            }
            notificationService.notify(task.getAssignedUser(), NotificationType.DEADLINE_APPROACHING,
                    "Task '" + task.getTitle() + "' is due tomorrow", task.getProject(), task);
        }
    }
}
