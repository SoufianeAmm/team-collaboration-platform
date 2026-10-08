package com.teamcollab.backend.dto;

import com.teamcollab.backend.entity.Task;

import java.util.Collection;

/** Powers the "Total Tasks: 24, To Do: 8, ..." summary on the project dashboard. */
public record TaskStatsResponse(long total, long todo, long inProgress, long inReview, long completed) {

    public static TaskStatsResponse from(Collection<Task> tasks) {
        long todo = 0, inProgress = 0, inReview = 0, completed = 0;
        for (Task task : tasks) {
            switch (task.getStatus()) {
                case TODO -> todo++;
                case IN_PROGRESS -> inProgress++;
                case IN_REVIEW -> inReview++;
                case COMPLETED -> completed++;
            }
        }
        return new TaskStatsResponse(tasks.size(), todo, inProgress, inReview, completed);
    }
}
