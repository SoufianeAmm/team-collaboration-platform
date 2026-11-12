import { api } from "./api";
import type { Role, User } from "../types/user";

export async function listUsers(): Promise<User[]> {
  const { data } = await api.get<User[]>("/users");
  return data;
}

export async function updateUserRole(id: number, role: Role): Promise<User> {
  const { data } = await api.patch<User>(`/users/${id}/role`, { role });
  return data;
}

export interface AdminUpdateUserRequest {
  firstName: string;
  lastName: string;
  email: string;
  password?: string;
}

export async function updateUser(id: number, request: AdminUpdateUserRequest): Promise<User> {
  const { data } = await api.patch<User>(`/users/${id}`, request);
  return data;
}

export async function deleteUser(id: number): Promise<void> {
  await api.delete(`/users/${id}`);
}

export async function updateProfile(firstName: string, lastName: string): Promise<User> {
  const { data } = await api.patch<User>("/users/me", { firstName, lastName });
  return data;
}

export async function uploadAvatar(file: File): Promise<User> {
  const formData = new FormData();
  formData.append("file", file);
  // Don't set Content-Type manually - axios/the browser need to add the multipart boundary themselves.
  const { data } = await api.post<User>("/users/me/avatar", formData);
  return data;
}

export async function removeAvatar(): Promise<User> {
  const { data } = await api.delete<User>("/users/me/avatar");
  return data;
}
