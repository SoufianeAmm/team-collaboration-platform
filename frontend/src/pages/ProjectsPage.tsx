import { useEffect, useState } from "react";
import type { FormEvent } from "react";
import { Link } from "react-router-dom";
import { FolderPlus, Plus } from "lucide-react";
import { EmptyState } from "../components/EmptyState";
import { Modal } from "../components/Modal";
import { ProjectStatusBadge } from "../components/badges";
import { useAuth } from "../context/AuthContext";
import { useToast } from "../context/ToastContext";
import { getErrorMessage } from "../services/api";
import * as projectService from "../services/projectService";
import type { Project } from "../types/project";

export function ProjectsPage() {
  const { user } = useAuth();
  const showToast = useToast();
  const [projects, setProjects] = useState<Project[] | null>(null);
  const [error, setError] = useState<string | null>(null);
  const [isModalOpen, setIsModalOpen] = useState(false);

  const canCreate = user?.role === "PROJECT_MANAGER" || user?.role === "ADMIN";

  function load() {
    projectService
      .listProjects()
      .then(setProjects)
      .catch((err) => setError(getErrorMessage(err)));
  }

  useEffect(load, []);

  function handleCreated(project: Project) {
    setProjects((prev) => [...(prev ?? []), project]);
    setIsModalOpen(false);
    showToast(`Project "${project.name}" created`);
  }

  return (
    <div>
      <div className="page-header">
        <div>
          <h1>Projects</h1>
          <p>Projects you own or are a member of.</p>
        </div>
        {canCreate && (
          <button className="btn btn-primary" onClick={() => setIsModalOpen(true)}>
            <Plus size={16} strokeWidth={2.4} />
            New Project
          </button>
        )}
      </div>

      {error && <div className="inline-error">{error}</div>}

      {projects === null ? (
        <div className="card">Loading...</div>
      ) : projects.length === 0 ? (
        <div className="card">
          <EmptyState
            icon={<FolderPlus size={24} />}
            title="No projects yet"
            description={
              canCreate
                ? "Create your first project to get started."
                : "You haven't been added to a project yet."
            }
          />
        </div>
      ) : (
        <div className="project-grid">
          {projects.map((project) => (
            <Link key={project.id} to={`/projects/${project.id}`} className="project-card">
              <div className="project-card-title">
                <h3>{project.name}</h3>
                <ProjectStatusBadge status={project.status} />
              </div>
              {project.description && <p className="project-card-desc">{project.description}</p>}
              <div className="project-card-footer">
                <span>{project.members.length} member{project.members.length === 1 ? "" : "s"}</span>
                <span>{project.taskStats.total} task{project.taskStats.total === 1 ? "" : "s"}</span>
              </div>
            </Link>
          ))}
        </div>
      )}

      {isModalOpen && (
        <CreateProjectModal onClose={() => setIsModalOpen(false)} onCreated={handleCreated} />
      )}
    </div>
  );
}

function CreateProjectModal({
  onClose,
  onCreated,
}: {
  onClose: () => void;
  onCreated: (project: Project) => void;
}) {
  const [name, setName] = useState("");
  const [description, setDescription] = useState("");
  const [error, setError] = useState<string | null>(null);
  const [isSubmitting, setIsSubmitting] = useState(false);

  async function handleSubmit(e: FormEvent) {
    e.preventDefault();
    setError(null);
    setIsSubmitting(true);
    try {
      const project = await projectService.createProject({ name, description });
      onCreated(project);
    } catch (err) {
      setError(getErrorMessage(err));
    } finally {
      setIsSubmitting(false);
    }
  }

  return (
    <Modal title="New Project" onClose={onClose}>
      {error && <div className="form-banner-error">{error}</div>}
      <form onSubmit={handleSubmit}>
        <div className="form-group">
          <label htmlFor="name">Name</label>
          <input id="name" value={name} onChange={(e) => setName(e.target.value)} required />
        </div>
        <div className="form-group">
          <label htmlFor="description">Description</label>
          <textarea
            id="description"
            rows={3}
            value={description}
            onChange={(e) => setDescription(e.target.value)}
          />
        </div>
        <div className="modal-actions">
          <button type="button" className="btn btn-secondary" onClick={onClose}>
            Cancel
          </button>
          <button type="submit" className="btn btn-primary" disabled={isSubmitting}>
            {isSubmitting ? "Creating..." : "Create Project"}
          </button>
        </div>
      </form>
    </Modal>
  );
}
