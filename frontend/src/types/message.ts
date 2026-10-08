import type { User } from "./user";

export interface ChatMessage {
  id: number;
  content: string;
  sender: User;
  projectId: number;
  createdAt: string;
}
