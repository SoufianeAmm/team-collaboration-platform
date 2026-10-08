import { useEffect, useState } from "react";
import { NavLink, Outlet, useOutletContext, useParams } from "react-router-dom";
import { ProjectStatusBadge } from "../components/badges";
import { getErrorMessage } from "../services/api";
import * as projectService from "../services/projectService";
import type { Project } from "../types/project";

interface ProjectContextValue {
  project: Project;
  refresh: () => void;
}

export function useProjectContext() {
  return useOutletContext<ProjectContextValue>();
}

const tabs = [
  { to: "", label: "Overview" },
  { to: "tasks", label: "Tasks" },
  { to: "chat", label: "Chat" },
  { to: "meeting", label: "Meeting" },
  { to: "files", label: "Files" },
];

export function ProjectLayout() {
  const { id } = useParams();
  const [project, setProject] = useState<Project | null>(null);
  const [error, setError] = useState<string | null>(null);

  function load() {
    if (!id) return;
    projectService
      .getProject(Number(id))
      .then(setProject)
      .catch((err) => setError(getErrorMessage(err)));
  }

  useEffect(load, [id]);

  if (error) {
    return <div className="card inline-error">{error}</div>;
  }

  if (!project) {
    return <div className="card">Loading...</div>;
  }

  return (
    <div>
      <div className="project-header">
        <div className="project-card-title">
          <h1>{project.name}</h1>
          <ProjectStatusBadge status={project.status} />
        </div>
      </div>
      <nav className="project-tabs">
        {tabs.map((tab) => (
          <NavLink
            key={tab.label}
            to={tab.to}
            end={tab.to === ""}
            className={({ isActive }) => `project-tab${isActive ? " active" : ""}`}
          >
            {tab.label}
          </NavLink>
        ))}
      </nav>
      <Outlet context={{ project, refresh: load } satisfies ProjectContextValue} />
    </div>
  );
}
