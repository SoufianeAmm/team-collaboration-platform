export type NotificationType =
  | "PROJECT_INVITE"
  | "TASK_ASSIGNED"
  | "TASK_STATUS_CHANGED"
  | "COMMENT_ADDED"
  | "MENTION"
  | "DEADLINE_APPROACHING";

export interface AppNotification {
  id: number;
  type: NotificationType;
  content: string;
  isRead: boolean;
  relatedProjectId: number | null;
  relatedTaskId: number | null;
  createdAt: string;
}
