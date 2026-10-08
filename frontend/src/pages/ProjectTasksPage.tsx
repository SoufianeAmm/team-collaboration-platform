import { useEffect, useState } from "react";
import type { DragEvent, FormEvent } from "react";
import { useNavigate } from "react-router-dom";
import { Plus } from "lucide-react";
import { Avatar } from "../components/Avatar";
import { Modal } from "../components/Modal";
import { PriorityBadge, taskStatusLabel } from "../components/badges";
import { useToast } from "../context/ToastContext";
import { useTaskSocket } from "../hooks/useTaskSocket";
import { getErrorMessage } from "../services/api";
import * as taskService from "../services/taskService";
import type { Task, TaskPriority, TaskStatus } from "../types/task";
import type { TaskEvent } from "../types/taskEvent";
import { useProjectContext } from "./ProjectLayout";

const COLUMNS: TaskStatus[] = ["TODO", "IN_PROGRESS", "IN_REVIEW", "COMPLETED"];

export function ProjectTasksPage() {
  const { project } = useProjectContext();
  const showToast = useToast();
  const navigate = useNavigate();
  const [tasks, setTasks] = useState<Task[] | null>(null);
  const [error, setError] = useState<string | null>(null);
  const [isModalOpen, setIsModalOpen] = useState(false);
  const [dragOverColumn, setDragOverColumn] = useState<TaskStatus | null>(null);

  function load() {
    taskService
      .listTasksForProject(project.id)
      .then(setTasks)
      .catch((err) => setError(getErrorMessage(err)));
  }

  useEffect(load, [project.id]);

  // Live updates from ANY source, including this user's own actions (the server echoes them
  // back over the same topic) - merging by id makes re-applying an update we already made locally
  // harmless, so there's no need to distinguish "mine" from "someone else's" here.
  useTaskSocket(project.id, (event: TaskEvent) => {
    setTasks((prev) => {
      if (!prev) return prev;
      if (event.type === "DELETED") {
        return prev.filter((t) => t.id !== event.taskId);
      }
      const task = event.task!;
      const exists = prev.some((t) => t.id === task.id);
      return exists ? prev.map((t) => (t.id === task.id ? task : t)) : [...prev, task];
    });
  });

  function handleCreated(task: Task) {
    // The WS "CREATED" echo for this same task can arrive before this REST response does -
    // dedupe by id so whichever shows up second doesn't produce a duplicate card.
    setTasks((prev) => (prev?.some((t) => t.id === task.id) ? prev : [...(prev ?? []), task]));
    setIsModalOpen(false);
    showToast(`Task "${task.title}" created`);
  }

  function handleDragStart(e: DragEvent<HTMLDivElement>, taskId: number) {
    e.dataTransfer.setData("text/plain", String(taskId));
  }

  async function handleDrop(e: DragEvent<HTMLDivElement>, newStatus: TaskStatus) {
    e.preventDefault();
    setDragOverColumn(null);
    const taskId = Number(e.dataTransfer.getData("text/plain"));
    const task = tasks?.find((t) => t.id === taskId);
    if (!task || task.status === newStatus) return;

    // Optimistic update - the drag feels instant, and we roll back if the server rejects it
    // (e.g. a USER who isn't the assignee dragging someone else's card).
    setTasks((prev) => prev!.map((t) => (t.id === taskId ? { ...t, status: newStatus } : t)));
    try {
      await taskService.updateTask(taskId, {
        title: task.title,
        description: task.description ?? undefined,
        status: newStatus,
        priority: task.priority,
        assignedUserId: task.assignedUser?.id ?? null,
        dueDate: task.dueDate ?? null,
      });
    } catch (err) {
      setTasks((prev) => prev!.map((t) => (t.id === taskId ? task : t)));
      showToast(getErrorMessage(err), "error");
    }
  }

  if (error) return <div className="inline-error">{error}</div>;
  if (!tasks) return <div className="card">Loading...</div>;

  return (
    <div>
      <div className="page-header">
        <div />
        <button className="btn btn-primary" onClick={() => setIsModalOpen(true)}>
          <Plus size={16} strokeWidth={2.4} />
          New Task
        </button>
      </div>

      <div className="kanban-board">
        {COLUMNS.map((status) => {
          const columnTasks = tasks.filter((t) => t.status === status);
          return (
            <div
              key={status}
              className={`kanban-column${dragOverColumn === status ? " drag-over" : ""}`}
              onDragOver={(e) => {
                e.preventDefault();
                setDragOverColumn(status);
              }}
              onDragLeave={() => setDragOverColumn(null)}
              onDrop={(e) => handleDrop(e, status)}
            >
              <div className="kanban-column-header">
                <span>{taskStatusLabel(status)}</span>
                <span>{columnTasks.length}</span>
              </div>
              {columnTasks.map((task) => (
                <div
                  key={task.id}
                  className="kanban-card"
                  draggable
                  onDragStart={(e) => handleDragStart(e, task.id)}
                  onClick={() => navigate(`/tasks/${task.id}`)}
                >
                  <div className="kanban-card-title">{task.title}</div>
                  <div className="kanban-card-footer">
                    <PriorityBadge priority={task.priority} />
                    {task.assignedUser && (
                      <Avatar user={task.assignedUser} style={{ marginLeft: 0 }} />
                    )}
                  </div>
                  {task.dueDate && <div className="kanban-card-due">Due {task.dueDate}</div>}
                </div>
              ))}
            </div>
          );
        })}
      </div>

      {isModalOpen && (
        <CreateTaskModal
          projectId={project.id}
          members={project.members}
          onClose={() => setIsModalOpen(false)}
          onCreated={handleCreated}
        />
      )}
    </div>
  );
}

function CreateTaskModal({
  projectId,
  members,
  onClose,
  onCreated,
}: {
  projectId: number;
  members: { id: number; firstName: string; lastName: string }[];
  onClose: () => void;
  onCreated: (task: Task) => void;
}) {
  const [title, setTitle] = useState("");
  const [description, setDescription] = useState("");
  const [priority, setPriority] = useState<TaskPriority>("MEDIUM");
  const [assignedUserId, setAssignedUserId] = useState("");
  const [dueDate, setDueDate] = useState("");
  const [error, setError] = useState<string | null>(null);
  const [isSubmitting, setIsSubmitting] = useState(false);

  async function handleSubmit(e: FormEvent) {
    e.preventDefault();
    setError(null);
    setIsSubmitting(true);
    try {
      const task = await taskService.createTask(projectId, {
        title,
        description: description || undefined,
        priority,
        assignedUserId: assignedUserId ? Number(assignedUserId) : undefined,
        dueDate: dueDate || undefined,
      });
      onCreated(task);
    } catch (err) {
      setError(getErrorMessage(err));
    } finally {
      setIsSubmitting(false);
    }
  }

  return (
    <Modal title="New Task" onClose={onClose}>
      {error && <div className="form-banner-error">{error}</div>}
      <form onSubmit={handleSubmit}>
        <div className="form-group">
          <label htmlFor="title">Title</label>
          <input id="title" value={title} onChange={(e) => setTitle(e.target.value)} required />
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
            {members.map((m) => (
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
        <div className="modal-actions">
          <button type="button" className="btn btn-secondary" onClick={onClose}>
            Cancel
          </button>
          <button type="submit" className="btn btn-primary" disabled={isSubmitting}>
            {isSubmitting ? "Creating..." : "Create Task"}
          </button>
        </div>
      </form>
    </Modal>
  );
}
