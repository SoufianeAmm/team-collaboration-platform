import { useEffect, useState } from "react";
import type { FormEvent } from "react";
import { Link, useNavigate, useParams } from "react-router-dom";
import { PriorityBadge } from "../components/badges";
import { CommentsSection } from "../components/CommentsSection";
import { useAuth } from "../context/AuthContext";
import { useToast } from "../context/ToastContext";
import { getErrorMessage } from "../services/api";
import * as projectService from "../services/projectService";
import * as taskService from "../services/taskService";
import type { Project } from "../types/project";
import type { Task, TaskPriority, TaskStatus } from "../types/task";

export function TaskDetailPage() {
  const { id } = useParams();
  const navigate = useNavigate();
  const showToast = useToast();
  const { user } = useAuth();
  const [task, setTask] = useState<Task | null>(null);
  const [project, setProject] = useState<Project | null>(null);
  const [error, setError] = useState<string | null>(null);

  const [title, setTitle] = useState("");
  const [description, setDescription] = useState("");
  const [status, setStatus] = useState<TaskStatus>("TODO");
  const [priority, setPriority] = useState<TaskPriority>("MEDIUM");
  const [assignedUserId, setAssignedUserId] = useState("");
  const [dueDate, setDueDate] = useState("");
  const [isSaving, setIsSaving] = useState(false);
  const [isDeleting, setIsDeleting] = useState(false);

  useEffect(() => {
    if (!id) return;
    taskService
      .getTask(Number(id))
      .then((t) => {
        setTask(t);
        setTitle(t.title);
        setDescription(t.description ?? "");
        setStatus(t.status);
        setPriority(t.priority);
        setAssignedUserId(t.assignedUser ? String(t.assignedUser.id) : "");
        setDueDate(t.dueDate ?? "");
        return projectService.getProject(t.projectId);
      })
      .then(setProject)
      .catch((err) => setError(getErrorMessage(err)));
  }, [id]);

  async function handleSave(e: FormEvent) {
    e.preventDefault();
    if (!task) return;
    setError(null);
    setIsSaving(true);
    try {
      const updated = await taskService.updateTask(task.id, {
        title,
        description: description || undefined,
        status,
        priority,
        assignedUserId: assignedUserId ? Number(assignedUserId) : null,
        dueDate: dueDate || null,
      });
      setTask(updated);
      showToast("Task saved");
    } catch (err) {
      setError(getErrorMessage(err));
    } finally {
      setIsSaving(false);
    }
  }

  async function handleDelete() {
    if (!task || !confirm(`Delete task "${task.title}"?`)) return;
    setIsDeleting(true);
    try {
      await taskService.deleteTask(task.id);
      showToast("Task deleted");
      navigate(`/projects/${task.projectId}/tasks`);
    } catch (err) {
      showToast(getErrorMessage(err), "error");
      setIsDeleting(false);
    }
  }

  if (error && !task) return <div className="inline-error">{error}</div>;
  if (!task || !project) return <div className="card">Loading...</div>;

  return (
    <div>
      <div className="page-header">
        <div>
          <Link to={`/projects/${task.projectId}/tasks`}>&larr; Back to {project.name}</Link>
          <h1 style={{ marginTop: 6 }}>{task.title}</h1>
          <PriorityBadge priority={task.priority} />
        </div>
      </div>

      {error && <div className="form-banner-error">{error}</div>}

      <div className="card" style={{ maxWidth: 520 }}>
        <form onSubmit={handleSave}>
          <div className="form-group">
            <label htmlFor="title">Title</label>
            <input id="title" value={title} onChange={(e) => setTitle(e.target.value)} required />
          </div>
          <div className="form-group">
            <label htmlFor="description">Description</label>
            <textarea
              id="description"
              rows={4}
              value={description}
              onChange={(e) => setDescription(e.target.value)}
            />
          </div>
          <div className="form-group">
            <label htmlFor="status">Status</label>
            <select id="status" value={status} onChange={(e) => setStatus(e.target.value as TaskStatus)}>
              <option value="TODO">To Do</option>
              <option value="IN_PROGRESS">In Progress</option>
              <option value="IN_REVIEW">In Review</option>
              <option value="COMPLETED">Completed</option>
            </select>
          </div>
          <div className="form-group">
            <label htmlFor="priority">Priority</label>
            <select id="priority" value={priority} onChange={(e) => setPriority(e.target.value as TaskPriority)}>
              <option value="LOW">Low</option>
              <option value="MEDIUM">Medium</option>
              <option value="HIGH">High</option>
              <option value="URGENT">Urgent</option>
            </select>
          </div>
          <div className="form-group">
            <label htmlFor="assignee">Assign to</label>
            <select id="assignee" value={assignedUserId} onChange={(e) => setAssignedUserId(e.target.value)}>
              <option value="">Unassigned</option>
              {project.members.map((m) => (
                <option key={m.id} value={m.id}>
                  {m.firstName} {m.lastName}
                </option>
              ))}
            </select>
          </div>
          <div className="form-group">
            <label htmlFor="dueDate">Due date</label>
            <input id="dueDate" type="date" value={dueDate} onChange={(e) => setDueDate(e.target.value)} />
          </div>
          <div className="modal-actions" style={{ justifyContent: "space-between" }}>
            <button type="button" className="btn btn-danger" onClick={handleDelete} disabled={isDeleting}>
              {isDeleting ? "Deleting..." : "Delete Task"}
            </button>
            <button type="submit" className="btn btn-primary" disabled={isSaving}>
              {isSaving ? "Saving..." : "Save Changes"}
            </button>
          </div>
        </form>
      </div>

      <CommentsSection
        taskId={task.id}
        canModerate={user?.role === "ADMIN" || user?.id === project.owner.id}
      />
    </div>
  );
}
