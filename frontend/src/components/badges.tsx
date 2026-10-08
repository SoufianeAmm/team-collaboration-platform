import type { ProjectStatus } from "../types/project";
import type { TaskPriority, TaskStatus } from "../types/task";
import type { Role } from "../types/user";

const PROJECT_STATUS_STYLE: Record<ProjectStatus, string> = {
  PLANNING: "badge-neutral",
  ACTIVE: "badge-accent",
  COMPLETED: "badge-success",
  ARCHIVED: "badge-neutral",
};

const TASK_STATUS_LABEL: Record<TaskStatus, string> = {
  TODO: "To Do",
  IN_PROGRESS: "In Progress",
  IN_REVIEW: "In Review",
  COMPLETED: "Completed",
};

const PRIORITY_STYLE: Record<TaskPriority, string> = {
  LOW: "badge-neutral",
  MEDIUM: "badge-accent",
  HIGH: "badge-warning",
  URGENT: "badge-danger",
};

const ROLE_STYLE: Record<Role, string> = {
  USER: "badge-neutral",
  PROJECT_MANAGER: "badge-accent",
  ADMIN: "badge-danger",
};

const ROLE_LABEL: Record<Role, string> = {
  USER: "Member",
  PROJECT_MANAGER: "Project Manager",
  ADMIN: "Admin",
};

export function ProjectStatusBadge({ status }: { status: ProjectStatus }) {
  return <span className={`badge ${PROJECT_STATUS_STYLE[status]}`}>{status}</span>;
}

export function PriorityBadge({ priority }: { priority: TaskPriority }) {
  return <span className={`badge ${PRIORITY_STYLE[priority]}`}>{priority}</span>;
}

export function RoleBadge({ role }: { role: Role }) {
  return <span className={`badge ${ROLE_STYLE[role]}`}>{ROLE_LABEL[role]}</span>;
}

export function taskStatusLabel(status: TaskStatus): string {
  return TASK_STATUS_LABEL[status];
}
