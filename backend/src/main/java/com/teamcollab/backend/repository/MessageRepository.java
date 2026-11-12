package com.teamcollab.backend.repository;

import com.teamcollab.backend.entity.Message;
import com.teamcollab.backend.entity.Project;
import com.teamcollab.backend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MessageRepository extends JpaRepository<Message, Long> {
    List<Message> findByProjectOrderByCreatedAtAsc(Project project);
    List<Message> findBySender(User sender);
}
