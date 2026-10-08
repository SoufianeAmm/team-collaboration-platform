package com.teamcollab.backend.repository;

import com.teamcollab.backend.entity.Project;
import com.teamcollab.backend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ProjectRepository extends JpaRepository<Project, Long> {
    List<Project> findByMembersContaining(User member);
    List<Project> findByOwner(User owner);

    // A pure DB-side check - avoids ever needing to touch the lazy `members` collection outside
    // a Hibernate session, which matters for callers like the WebSocket interceptor that run
    // on a plain message-handling thread, not inside a normal HTTP request.
    @Query("""
            SELECT CASE WHEN COUNT(p) > 0 THEN true ELSE false END FROM Project p
            WHERE p.id = :projectId
              AND (p.owner.id = :userId OR :userId IN (SELECT m.id FROM p.members m))
            """)
    boolean isMember(@Param("projectId") Long projectId, @Param("userId") Long userId);

    // Same reasoning as isMember() above - a dedicated query instead of touching the lazy
    // `members` collection, so this is safe to call from the WebSocket/STOMP message path too
    // (used for resolving @mentions in comments and chat messages).
    @Query("SELECT m FROM Project p JOIN p.members m WHERE p.id = :projectId")
    List<User> findMembers(@Param("projectId") Long projectId);
}
