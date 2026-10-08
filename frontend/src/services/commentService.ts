import { api } from "./api";
import type { Comment } from "../types/comment";

export async function listComments(taskId: number): Promise<Comment[]> {
  const { data } = await api.get<Comment[]>(`/tasks/${taskId}/comments`);
  return data;
}

export async function createComment(taskId: number, content: string): Promise<Comment> {
  const { data } = await api.post<Comment>(`/tasks/${taskId}/comments`, { content });
  return data;
}

export async function deleteComment(commentId: number): Promise<void> {
  await api.delete(`/comments/${commentId}`);
}
