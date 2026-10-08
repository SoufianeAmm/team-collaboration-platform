package com.teamcollab.backend.dto;

/**
 * Pushed over /topic/projects/{id}/tasks whenever a task is created/updated/deleted via REST.
 * REST remains the only way to actually change a task - this is purely a "something changed,
 * here's the new state" notification so other viewers' Kanban boards can update live.
 */
public record TaskEvent(Type type, TaskResponse task, Long taskId) {

    public enum Type { CREATED, UPDATED, DELETED }

    public static TaskEvent created(TaskResponse task) {
        return new TaskEvent(Type.CREATED, task, task.id());
    }

    public static TaskEvent updated(TaskResponse task) {
        return new TaskEvent(Type.UPDATED, task, task.id());
    }

    public static TaskEvent deleted(Long taskId) {
        return new TaskEvent(Type.DELETED, null, taskId);
    }
}
