import { api } from "./api";
import type { AppNotification } from "../types/notification";

export async function listNotifications(): Promise<AppNotification[]> {
  const { data } = await api.get<AppNotification[]>("/notifications");
  return data;
}

export async function markAsRead(id: number): Promise<AppNotification> {
  const { data } = await api.put<AppNotification>(`/notifications/${id}/read`);
  return data;
}
