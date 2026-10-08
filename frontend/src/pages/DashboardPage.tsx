import { useEffect, useState } from "react";
import { Link } from "react-router-dom";
import { FolderKanban, ListChecks, Activity, LayoutDashboard } from "lucide-react";
import { useAuth } from "../context/AuthContext";
import { EmptyState } from "../components/EmptyState";
import { ProjectStatusBadge } from "../components/badges";
import * as projectService from "../services/projectService";
import type { Project } from "../types/project";

export function DashboardPage() {
  const { user } = useAuth();
  const [projects, setProjects] = useState<Project[] | null>(null);

  useEffect(() => {
    projectService.listProjects().then(setProjects).catch(() => setProjects([]));
  }, []);

  const totalTasks = projects?.reduce((sum, p) => sum + p.taskStats.total, 0) ?? 0;
  const inProgress = projects?.reduce((sum, p) => sum + p.taskStats.inProgress, 0) ?? 0;

  return (
    <div>
      <div className="page-header">
        <div>
          <h1>Welcome back, {user?.firstName}</h1>
          <p>Here's what's happening across your projects.</p>
        </div>
      </div>

      {projects && projects.length > 0 && (
        <div className="stat-grid">
          <div className="stat-card">
            <div>
              <div className="stat-value">{projects.length}</div>
              <div className="stat-label">Projects</div>
            </div>
            <div className="stat-card-icon">
              <FolderKanban size={18} strokeWidth={2.2} />
            </div>
          </div>
          <div className="stat-card">
            <div>
              <div className="stat-value">{totalTasks}</div>
              <div className="stat-label">Total Tasks</div>
            </div>
            <div className="stat-card-icon">
              <ListChecks size={18} strokeWidth={2.2} />
            </div>
          </div>
          <div className="stat-card">
            <div>
              <div className="stat-value">{inProgress}</div>
              <div className="stat-label">In Progress</div>
            </div>
            <div className="stat-card-icon">
              <Activity size={18} strokeWidth={2.2} />
            </div>
          </div>
        </div>
      )}

      <div className="card">
        {projects === null ? (
          "Loading..."
        ) : projects.length === 0 ? (
          <EmptyState
            icon={<LayoutDashboard size={24} />}
            title="Your dashboard is empty"
            description="Create or join a project to see your task stats and recent activity here."
            action={
              <Link to="/projects" className="btn btn-primary">
                Go to Projects
              </Link>
            }
          />
        ) : (
          <>
            <h2 style={{ marginBottom: 12 }}>Your Projects</h2>
            <div className="project-grid">
              {projects.map((project) => (
                <Link key={project.id} to={`/projects/${project.id}`} className="project-card">
                  <div className="project-card-title">
                    <h3>{project.name}</h3>
                    <ProjectStatusBadge status={project.status} />
                  </div>
                  <div className="project-card-footer">
                    <span>{project.members.length} member{project.members.length === 1 ? "" : "s"}</span>
                    <span>{project.taskStats.total} task{project.taskStats.total === 1 ? "" : "s"}</span>
                  </div>
                </Link>
              ))}
            </div>
          </>
        )}
      </div>
    </div>
  );
}
