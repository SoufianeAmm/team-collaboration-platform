import { useEffect, useRef } from "react";
import { Client } from "@stomp/stompjs";
import type { TaskEvent } from "../types/taskEvent";
import { getWebSocketUrl } from "../services/wsUrl";

/**
 * Subscribes to a project's task-event topic and calls onEvent for every CREATED/UPDATED/DELETED
 * event - including ones triggered by the current user's own actions (the REST call that made
 * the change gets echoed back over this same socket). The caller's merge logic just needs to be
 * idempotent, which keeping state keyed by task id naturally is.
 */
export function useTaskSocket(projectId: number, onEvent: (event: TaskEvent) => void) {
  const onEventRef = useRef(onEvent);
  onEventRef.current = onEvent;

  useEffect(() => {
    const token = localStorage.getItem("token");
    if (!token) return;

    const client = new Client({
      brokerURL: getWebSocketUrl(),
      connectHeaders: { Authorization: `Bearer ${token}` },
      reconnectDelay: 3000,
      onConnect: () => {
        client.subscribe(`/topic/projects/${projectId}/tasks`, (frame) => {
          onEventRef.current(JSON.parse(frame.body));
        });
      },
    });

    client.activate();
    return () => {
      client.deactivate();
    };
  }, [projectId]);
}
