import { useNavigate } from "react-router-dom";
import { Bell, FolderOpen, Pin, RefreshCw, MessageSquare, Clock } from "lucide-react";
import { EmptyState } from "../components/EmptyState";
import { useNotifications } from "../context/NotificationContext";
import type { AppNotification } from "../types/notification";

const TYPE_ICON: Record<AppNotification["type"], typeof FolderOpen> = {
  PROJECT_INVITE: FolderOpen,
  TASK_ASSIGNED: Pin,
  TASK_STATUS_CHANGED: RefreshCw,
  COMMENT_ADDED: MessageSquare,
  MENTION: MessageSquare,
  DEADLINE_APPROACHING: Clock,
};

export function NotificationsPage() {
  const { notifications, markRead } = useNotifications();
  const navigate = useNavigate();

  function handleClick(n: AppNotification) {
    if (!n.isRead) markRead(n.id);
    if (n.relatedTaskId) {
      navigate(`/tasks/${n.relatedTaskId}`);
    } else if (n.relatedProjectId) {
      navigate(`/projects/${n.relatedProjectId}`);
    }
  }

  return (
    <div>
      <div className="page-header">
        <h1>Notifications</h1>
      </div>

      {notifications.length === 0 ? (
        <div className="card">
          <EmptyState icon={<Bell size={24} />} title="No notifications yet" description="You're all caught up." />
        </div>
      ) : (
        <div className="card" style={{ padding: 0 }}>
          {notifications.map((n) => {
            const Icon = TYPE_ICON[n.type];
            return (
              <button
                key={n.id}
                className={`notification-item${n.isRead ? "" : " unread"}`}
                style={{ width: "100%" }}
                onClick={() => handleClick(n)}
              >
                <span className="notification-icon">
                  <Icon size={15} strokeWidth={2.2} />
                </span>
                <span className="notification-text">
                  <span>{n.content}</span>
                  <span className="notification-time">{new Date(n.createdAt).toLocaleString()}</span>
                </span>
              </button>
            );
          })}
        </div>
      )}
    </div>
  );
}
