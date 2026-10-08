import { useEffect, useRef, useState } from "react";
import { Client } from "@stomp/stompjs";
import type { ChatMessage } from "../types/message";
import { getWebSocketUrl } from "../services/wsUrl";

/**
 * Owns the WebSocket/STOMP connection for one project's chat. REST (messageService) is used
 * once up front to load history; after that, every new message arrives over this socket -
 * no polling.
 */
export function useChatSocket(projectId: number) {
  const [messages, setMessages] = useState<ChatMessage[]>([]);
  const [connected, setConnected] = useState(false);
  const clientRef = useRef<Client | null>(null);

  useEffect(() => {
    setMessages([]);

    const token = localStorage.getItem("token");
    if (!token) return;

    const client = new Client({
      brokerURL: getWebSocketUrl(),
      connectHeaders: { Authorization: `Bearer ${token}` },
      reconnectDelay: 3000,
      onConnect: () => {
        setConnected(true);
        client.subscribe(`/topic/projects/${projectId}/chat`, (frame) => {
          const message: ChatMessage = JSON.parse(frame.body);
          setMessages((prev) => [...prev, message]);
        });
      },
      onWebSocketClose: () => setConnected(false),
    });

    client.activate();
    clientRef.current = client;

    return () => {
      client.deactivate();
      clientRef.current = null;
    };
  }, [projectId]);

  function sendMessage(content: string) {
    clientRef.current?.publish({
      destination: `/app/chat.send/${projectId}`,
      body: JSON.stringify({ content }),
    });
  }

  function prependHistory(history: ChatMessage[]) {
    setMessages((prev) => [...history, ...prev]);
  }

  return { messages, connected, sendMessage, prependHistory };
}
