import { useMemo, useState } from "react";
import { Video, ExternalLink } from "lucide-react";
import { useAuth } from "../context/AuthContext";
import { useProjectContext } from "./ProjectLayout";

// A public meet.jit.si room, scoped to this project. No backend involved - Jitsi hosts the
// call entirely; we just need both ends to land on the same (hard-to-guess) room name.
function roomNameFor(projectId: number, projectName: string): string {
  const slug = projectName
    .toLowerCase()
    .replace(/[^a-z0-9]+/g, "-")
    .replace(/(^-|-$)/g, "");
  return `teamcollab-${projectId}-${slug}`;
}

export function ProjectMeetingPage() {
  const { project } = useProjectContext();
  const { user } = useAuth();
  const [hasJoined, setHasJoined] = useState(false);

  const room = useMemo(() => roomNameFor(project.id, project.name), [project.id, project.name]);
  const displayName = user ? `${user.firstName} ${user.lastName}` : "";
  const meetingUrl = `https://meet.jit.si/${room}#userInfo.displayName="${encodeURIComponent(displayName)}"`;

  if (!hasJoined) {
    return (
      <div className="card">
        <div className="empty-state">
          <div className="empty-state-icon">
            <Video size={24} />
          </div>
          <h3>Start a meeting</h3>
          <p>
            Everyone on "{project.name}" who opens this tab joins the same call. Powered by Jitsi
            Meet - no account needed, camera/mic permission is only requested once you join.
          </p>
          <button className="btn btn-primary" style={{ marginTop: 16 }} onClick={() => setHasJoined(true)}>
            <Video size={15} strokeWidth={2.2} />
            Join Meeting
          </button>
        </div>
      </div>
    );
  }

  return (
    <div className="card meeting-card">
      <div className="meeting-toolbar">
        <span className="meeting-room-name">{room}</span>
        <a
          className="btn btn-secondary"
          href={meetingUrl}
          target="_blank"
          rel="noreferrer"
        >
          <ExternalLink size={14} strokeWidth={2.2} />
          Open in new tab
        </a>
      </div>
      <iframe
        key={room}
        src={meetingUrl}
        allow="camera; microphone; display-capture; fullscreen; clipboard-write"
        className="meeting-frame"
        title={`Meeting for ${project.name}`}
      />
    </div>
  );
}
