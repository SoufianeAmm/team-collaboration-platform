import { useState } from "react";
import type { FormEvent } from "react";
import { useNavigate } from "react-router-dom";
import { ListTodo, Circle, Loader2, CheckCircle2, UserPlus } from "lucide-react";
import { Avatar } from "../components/Avatar";
import { Modal } from "../components/Modal";
import { useAuth } from "../context/AuthContext";
import { useToast } from "../context/ToastContext";
import { getErrorMessage } from "../services/api";
import * as projectService from "../services/projectService";
import { useProjectContext } from "./ProjectLayout";

export function ProjectOverviewPage() {
  const { project, refresh } = useProjectContext();
  const { user } = useAuth();
  const showToast = useToast();
  const navigate = useNavigate();
  const [isAddMemberOpen, setIsAddMemberOpen] = useState(false);
  const [isDeleting, setIsDeleting] = useState(false);

  const isOwnerOrAdmin = user?.role === "ADMIN" || user?.id === project.owner.id;
  const { taskStats } = project;

  async function handleRemoveMember(userId: number) {
    try {
      await projectService.removeMember(project.id, userId);
      refresh();
      showToast("Member removed");
    } catch (err) {
      showToast(getErrorMessage(err), "error");
    }
  }

  async function handleDeleteProject() {
    if (!confirm(`Delete "${project.name}"? This cannot be undone.`)) return;
    setIsDeleting(true);
    try {
      await projectService.deleteProject(project.id);
      showToast("Project deleted");
      navigate("/projects");
    } catch (err) {
      showToast(getErrorMessage(err), "error");
      setIsDeleting(false);
    }
  }

  return (
    <div>
      <div className="stat-grid">
        <div className="stat-card">
          <div>
            <div className="stat-value">{taskStats.total}</div>
            <div className="stat-label">Total Tasks</div>
          </div>
          <div className="stat-card-icon">
            <ListTodo size={18} strokeWidth={2.2} />
          </div>
        </div>
        <div className="stat-card">
          <div>
            <div className="stat-value">{taskStats.todo}</div>
            <div className="stat-label">To Do</div>
          </div>
          <div className="stat-card-icon">
            <Circle size={18} strokeWidth={2.2} />
          </div>
        </div>
        <div className="stat-card">
          <div>
            <div className="stat-value">{taskStats.inProgress}</div>
            <div className="stat-label">In Progress</div>
          </div>
          <div className="stat-card-icon">
            <Loader2 size={18} strokeWidth={2.2} />
          </div>
        </div>
        <div className="stat-card">
          <div>
            <div className="stat-value">{taskStats.completed}</div>
            <div className="stat-label">Completed</div>
          </div>
          <div className="stat-card-icon">
            <CheckCircle2 size={18} strokeWidth={2.2} />
          </div>
        </div>
      </div>

      <div className="card" style={{ marginBottom: 16 }}>
        <h2 style={{ marginBottom: 8 }}>Description</h2>
        <p>{project.description || "No description provided."}</p>
      </div>

      <div className="card">
        <div className="page-header" style={{ marginBottom: 12 }}>
          <h2>Members</h2>
          {isOwnerOrAdmin && (
            <button className="btn btn-secondary" onClick={() => setIsAddMemberOpen(true)}>
              <UserPlus size={15} strokeWidth={2.2} />
              Add Member
            </button>
          )}
        </div>
        {project.members.map((member) => (
          <div key={member.id} className="member-row">
            <div className="member-row-info">
              <Avatar user={member} style={{ marginLeft: 0 }} />
              <span>
                {member.firstName} {member.lastName}
                {member.id === project.owner.id && " (Owner)"}
              </span>
            </div>
            {isOwnerOrAdmin && member.id !== project.owner.id && (
              <button className="btn btn-danger" onClick={() => handleRemoveMember(member.id)}>
                Remove
              </button>
            )}
          </div>
        ))}
      </div>

      {isOwnerOrAdmin && (
        <div style={{ marginTop: 16 }}>
          <button className="btn btn-danger" onClick={handleDeleteProject} disabled={isDeleting}>
            {isDeleting ? "Deleting..." : "Delete Project"}
          </button>
        </div>
      )}

      {isAddMemberOpen && (
        <AddMemberModal
          projectId={project.id}
          onClose={() => setIsAddMemberOpen(false)}
          onAdded={() => {
            refresh();
            setIsAddMemberOpen(false);
            showToast("Member added");
          }}
        />
      )}
    </div>
  );
}

function AddMemberModal({
  projectId,
  onClose,
  onAdded,
}: {
  projectId: number;
  onClose: () => void;
  onAdded: () => void;
}) {
  const [email, setEmail] = useState("");
  const [error, setError] = useState<string | null>(null);
  const [isSubmitting, setIsSubmitting] = useState(false);

  async function handleSubmit(e: FormEvent) {
    e.preventDefault();
    setError(null);
    setIsSubmitting(true);
    try {
      await projectService.addMember(projectId, email);
      onAdded();
    } catch (err) {
      setError(getErrorMessage(err));
    } finally {
      setIsSubmitting(false);
    }
  }

  return (
    <Modal title="Add Member" onClose={onClose}>
      {error && <div className="form-banner-error">{error}</div>}
      <form onSubmit={handleSubmit}>
        <div className="form-group">
          <label htmlFor="memberEmail">User's email</label>
          <input
            id="memberEmail"
            type="email"
            value={email}
            onChange={(e) => setEmail(e.target.value)}
            required
          />
        </div>
        <div className="modal-actions">
          <button type="button" className="btn btn-secondary" onClick={onClose}>
            Cancel
          </button>
          <button type="submit" className="btn btn-primary" disabled={isSubmitting}>
            {isSubmitting ? "Adding..." : "Add"}
          </button>
        </div>
      </form>
    </Modal>
  );
}
