import type { User } from "./user";

export type TaskStatus = "TODO" | "IN_PROGRESS" | "IN_REVIEW" | "COMPLETED";
export type TaskPriority = "LOW" | "MEDIUM" | "HIGH" | "URGENT";

export interface Task {
  id: number;
  title: string;
  description: string | null;
  status: TaskStatus;
  priority: TaskPriority;
  assignedUser: User | null;
  projectId: number;
  createdBy: User;
  dueDate: string | null;
  createdAt: string;
  updatedAt: string;
}

export interface TaskRequest {
  title: string;
  description?: string;
  assignedUserId?: number;
  priority?: TaskPriority;
  dueDate?: string;
}

export interface TaskUpdateRequest {
  title: string;
  description?: string;
  status: TaskStatus;
  priority: TaskPriority;
  assignedUserId?: number | null;
  dueDate?: string | null;
}
