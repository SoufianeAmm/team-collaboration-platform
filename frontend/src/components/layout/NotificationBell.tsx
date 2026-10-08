import { useState } from "react";
import { useNavigate } from "react-router-dom";
import { Bell, FolderOpen, Pin, RefreshCw, MessageSquare, Clock } from "lucide-react";
import { useNotifications } from "../../context/NotificationContext";
import type { AppNotification } from "../../types/notification";

const TYPE_ICON: Record<AppNotification["type"], typeof FolderOpen> = {
  PROJECT_INVITE: FolderOpen,
  TASK_ASSIGNED: Pin,
  TASK_STATUS_CHANGED: RefreshCw,
  COMMENT_ADDED: MessageSquare,
  MENTION: MessageSquare,
  DEADLINE_APPROACHING: Clock,
};

export function NotificationBell() {
  const { notifications, unreadCount, markRead } = useNotifications();
  const [isOpen, setIsOpen] = useState(false);
  const navigate = useNavigate();

  function handleClick(n: AppNotification) {
    if (!n.isRead) markRead(n.id);
    setIsOpen(false);
    if (n.relatedTaskId) {
      navigate(`/tasks/${n.relatedTaskId}`);
    } else if (n.relatedProjectId) {
      navigate(`/projects/${n.relatedProjectId}`);
    }
  }

  return (
    <div className="notification-bell-wrap">
      <button className="notification-bell" onClick={() => setIsOpen((v) => !v)} aria-label="Notifications">
        <Bell size={19} strokeWidth={2.1} />
        {unreadCount > 0 && <span className="notification-badge">{unreadCount}</span>}
      </button>

      {isOpen && (
        <>
          <div className="notification-backdrop" onClick={() => setIsOpen(false)} />
          <div className="notification-dropdown">
            {notifications.length === 0 ? (
              <div className="notification-empty">No notifications yet</div>
            ) : (
              notifications.slice(0, 10).map((n) => {
                const Icon = TYPE_ICON[n.type];
                return (
                  <button
                    key={n.id}
                    className={`notification-item${n.isRead ? "" : " unread"}`}
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
              })
            )}
          </div>
        </>
      )}
    </div>
  );
}
