import { createContext, useContext, useEffect, useState } from "react";
import type { ReactNode } from "react";
import { useAuth } from "./AuthContext";
import { useNotificationSocket } from "../hooks/useNotificationSocket";
import * as notificationService from "../services/notificationService";
import type { AppNotification } from "../types/notification";

interface NotificationContextValue {
  notifications: AppNotification[];
  unreadCount: number;
  markRead: (id: number) => void;
}

const NotificationContext = createContext<NotificationContextValue | undefined>(undefined);

export function NotificationProvider({ children }: { children: ReactNode }) {
  const { user } = useAuth();
  const [notifications, setNotifications] = useState<AppNotification[]>([]);

  useEffect(() => {
    if (!user) {
      setNotifications([]);
      return;
    }
    notificationService.listNotifications().then(setNotifications).catch(() => {});
  }, [user?.id]);

  useNotificationSocket((n) => {
    setNotifications((prev) => [n, ...prev]);
  }, !!user);

  async function markRead(id: number) {
    setNotifications((prev) => prev.map((n) => (n.id === id ? { ...n, isRead: true } : n)));
    try {
      await notificationService.markAsRead(id);
    } catch {
      // Leave it marked read locally - not worth bothering the user over a failed read-receipt.
    }
  }

  const unreadCount = notifications.filter((n) => !n.isRead).length;

  return (
    <NotificationContext.Provider value={{ notifications, unreadCount, markRead }}>
      {children}
    </NotificationContext.Provider>
  );
}

export function useNotifications(): NotificationContextValue {
  const context = useContext(NotificationContext);
  if (!context) {
    throw new Error("useNotifications must be used within a NotificationProvider");
  }
  return context;
}
