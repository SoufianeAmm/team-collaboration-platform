package com.teamcollab.backend.repository;

import com.teamcollab.backend.entity.Comment;
import com.teamcollab.backend.entity.Task;
import com.teamcollab.backend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CommentRepository extends JpaRepository<Comment, Long> {
    List<Comment> findByTask(Task task);
    List<Comment> findByAuthor(User author);
}
