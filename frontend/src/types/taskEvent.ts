import type { Task } from "./task";

export interface TaskEvent {
  type: "CREATED" | "UPDATED" | "DELETED";
  task: Task | null;
  taskId: number;
}
