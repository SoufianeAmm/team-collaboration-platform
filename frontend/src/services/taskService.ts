import { api } from "./api";
import type { Task, TaskRequest, TaskUpdateRequest } from "../types/task";

export async function listTasksForProject(projectId: number): Promise<Task[]> {
  const { data } = await api.get<Task[]>(`/projects/${projectId}/tasks`);
  return data;
}

export async function getTask(id: number): Promise<Task> {
  const { data } = await api.get<Task>(`/tasks/${id}`);
  return data;
}

export async function createTask(projectId: number, request: TaskRequest): Promise<Task> {
  const { data } = await api.post<Task>(`/projects/${projectId}/tasks`, request);
  return data;
}

export async function updateTask(id: number, request: TaskUpdateRequest): Promise<Task> {
  const { data } = await api.put<Task>(`/tasks/${id}`, request);
  return data;
}

export async function deleteTask(id: number): Promise<void> {
  await api.delete(`/tasks/${id}`);
}
