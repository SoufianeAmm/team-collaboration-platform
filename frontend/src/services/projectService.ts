import { api } from "./api";
import type { Project, ProjectRequest } from "../types/project";
import type { User } from "../types/user";

export async function listProjects(): Promise<Project[]> {
  const { data } = await api.get<Project[]>("/projects");
  return data;
}

export async function getProject(id: number): Promise<Project> {
  const { data } = await api.get<Project>(`/projects/${id}`);
  return data;
}

export async function createProject(request: ProjectRequest): Promise<Project> {
  const { data } = await api.post<Project>("/projects", request);
  return data;
}

export async function updateProject(id: number, request: ProjectRequest): Promise<Project> {
  const { data } = await api.put<Project>(`/projects/${id}`, request);
  return data;
}

export async function deleteProject(id: number): Promise<void> {
  await api.delete(`/projects/${id}`);
}

export async function addMember(projectId: number, email: string): Promise<User[]> {
  const { data } = await api.post<User[]>(`/projects/${projectId}/members`, { email });
  return data;
}

export async function removeMember(projectId: number, userId: number): Promise<User[]> {
  const { data } = await api.delete<User[]>(`/projects/${projectId}/members/${userId}`);
  return data;
}
