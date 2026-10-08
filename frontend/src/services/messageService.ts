import { api } from "./api";
import type { ChatMessage } from "../types/message";

/** Chat HISTORY only - a normal REST GET. New messages arrive live over the WebSocket instead. */
export async function listMessages(projectId: number): Promise<ChatMessage[]> {
  const { data } = await api.get<ChatMessage[]>(`/projects/${projectId}/messages`);
  return data;
}
