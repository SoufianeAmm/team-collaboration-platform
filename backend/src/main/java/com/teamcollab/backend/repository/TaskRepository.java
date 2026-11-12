package com.teamcollab.backend.repository;

import com.teamcollab.backend.entity.Project;
import com.teamcollab.backend.entity.Task;
import com.teamcollab.backend.entity.TaskStatus;
import com.teamcollab.backend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface TaskRepository extends JpaRepository<Task, Long> {
    List<Task> findByProject(Project project);
    List<Task> findByAssignedUser(User assignedUser);
    List<Task> findByCreatedBy(User createdBy);

    // Powers the deadline-approaching scheduled job.
    List<Task> findByDueDateAndStatusNot(LocalDate dueDate, TaskStatus excludedStatus);
}
