import { useEffect, useRef, useState } from "react";
import type { FormEvent } from "react";
import { Avatar } from "../components/Avatar";
import { useAuth } from "../context/AuthContext";
import { useChatSocket } from "../hooks/useChatSocket";
import * as messageService from "../services/messageService";
import { useProjectContext } from "./ProjectLayout";

export function ProjectChatPage() {
  const { project } = useProjectContext();
  const { user } = useAuth();
  const { messages, connected, sendMessage, prependHistory } = useChatSocket(project.id);
  const [draft, setDraft] = useState("");
  const bottomRef = useRef<HTMLDivElement>(null);

  useEffect(() => {
    messageService.listMessages(project.id).then(prependHistory);
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [project.id]);

  useEffect(() => {
    bottomRef.current?.scrollIntoView({ behavior: "smooth" });
  }, [messages]);

  function handleSend(e: FormEvent) {
    e.preventDefault();
    if (!draft.trim()) return;
    sendMessage(draft);
    setDraft("");
  }

  return (
    <div className="card chat-card">
      <div className="chat-status">
        <span className={`chat-status-dot${connected ? " connected" : ""}`} />
        {connected ? "Live" : "Connecting..."}
      </div>

      <div className="chat-log">
        {messages.length === 0 && <p style={{ color: "var(--text-muted)" }}>No messages yet. Say hello!</p>}
        {messages.map((msg) => {
          const isMe = msg.sender.id === user?.id;
          return (
            <div key={msg.id} className={`chat-message${isMe ? " chat-message-mine" : ""}`}>
              <Avatar user={msg.sender} />
              <div>
                <div className="chat-message-meta">
                  <strong>{isMe ? "You" : msg.sender.firstName}</strong>
                  <span>{new Date(msg.createdAt).toLocaleTimeString([], { hour: "2-digit", minute: "2-digit" })}</span>
                </div>
                <p>{msg.content}</p>
              </div>
            </div>
          );
        })}
        <div ref={bottomRef} />
      </div>

      <form className="chat-input-row" onSubmit={handleSend}>
        <input
          placeholder="Type a message..."
          value={draft}
          onChange={(e) => setDraft(e.target.value)}
          disabled={!connected}
        />
        <button type="submit" className="btn btn-primary" disabled={!connected || !draft.trim()}>
          Send
        </button>
      </form>
    </div>
  );
}
