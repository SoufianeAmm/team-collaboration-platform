import { useEffect, useRef } from "react";
import { Client } from "@stomp/stompjs";
import type { AppNotification } from "../types/notification";
import { getWebSocketUrl } from "../services/wsUrl";

/**
 * Subscribes to this user's own notification queue. "/user/queue/notifications" is Spring's
 * per-user destination convention - the server routes to it with convertAndSendToUser(), and
 * only this user's own session ever receives what's sent there (no project/topic involved).
 *
 * Unlike the chat/task sockets (only ever mounted inside already-authenticated pages), this one
 * lives at the app root and is mounted before login too - so it needs `enabled` to flip from
 * false to true at login time and actually (re)connect, not just check once at first mount.
 */
export function useNotificationSocket(onNotification: (n: AppNotification) => void, enabled: boolean) {
  const onNotificationRef = useRef(onNotification);
  onNotificationRef.current = onNotification;

  useEffect(() => {
    if (!enabled) return;
    const token = localStorage.getItem("token");
    if (!token) return;

    const client = new Client({
      brokerURL: getWebSocketUrl(),
      connectHeaders: { Authorization: `Bearer ${token}` },
      reconnectDelay: 3000,
      onConnect: () => {
        client.subscribe("/user/queue/notifications", (frame) => {
          onNotificationRef.current(JSON.parse(frame.body));
        });
      },
    });

    client.activate();
    return () => {
      client.deactivate();
    };
  }, [enabled]);
}
