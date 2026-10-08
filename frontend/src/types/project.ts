import type { User } from "./user";

export type ProjectStatus = "PLANNING" | "ACTIVE" | "COMPLETED" | "ARCHIVED";

export interface TaskStats {
  total: number;
  todo: number;
  inProgress: number;
  inReview: number;
  completed: number;
}

export interface Project {
  id: number;
  name: string;
  description: string | null;
  owner: User;
  status: ProjectStatus;
  members: User[];
  taskStats: TaskStats;
  createdAt: string;
  updatedAt: string;
}

export interface ProjectRequest {
  name: string;
  description?: string;
  status?: ProjectStatus;
}
