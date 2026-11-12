import { useEffect, useState } from "react";
import type { FormEvent } from "react";
import { Users, ShieldCheck, Briefcase, Pencil, Trash2 } from "lucide-react";
import { Avatar } from "../components/Avatar";
import { Modal } from "../components/Modal";
import { useAuth } from "../context/AuthContext";
import { useToast } from "../context/ToastContext";
import { getErrorMessage } from "../services/api";
import * as userService from "../services/userService";
import type { Role, User } from "../types/user";

const ROLE_OPTIONS: Role[] = ["USER", "PROJECT_MANAGER", "ADMIN"];
const ROLE_OPTION_LABEL: Record<Role, string> = {
  USER: "Member",
  PROJECT_MANAGER: "Project Manager",
  ADMIN: "Admin",
};

export function AdminPage() {
  const { user: me } = useAuth();
  const showToast = useToast();
  const [users, setUsers] = useState<User[] | null>(null);
  const [error, setError] = useState<string | null>(null);
  const [savingId, setSavingId] = useState<number | null>(null);
  const [editingUser, setEditingUser] = useState<User | null>(null);

  function load() {
    userService
      .listUsers()
      .then(setUsers)
      .catch((err) => setError(getErrorMessage(err)));
  }

  useEffect(load, []);

  async function handleRoleChange(targetUser: User, role: Role) {
    if (role === targetUser.role) return;
    setSavingId(targetUser.id);
    try {
      const updated = await userService.updateUserRole(targetUser.id, role);
      setUsers((prev) => prev!.map((u) => (u.id === updated.id ? updated : u)));
      showToast(`${updated.firstName} ${updated.lastName} is now ${ROLE_OPTION_LABEL[updated.role]}`);
    } catch (err) {
      showToast(getErrorMessage(err), "error");
    } finally {
      setSavingId(null);
    }
  }

  async function handleDelete(targetUser: User) {
    if (!confirm(`Delete ${targetUser.firstName} ${targetUser.lastName}? This cannot be undone.`)) return;
    setSavingId(targetUser.id);
    try {
      await userService.deleteUser(targetUser.id);
      setUsers((prev) => prev!.filter((u) => u.id !== targetUser.id));
      showToast(`${targetUser.firstName} ${targetUser.lastName} deleted`);
    } catch (err) {
      showToast(getErrorMessage(err), "error");
    } finally {
      setSavingId(null);
    }
  }

  const adminCount = users?.filter((u) => u.role === "ADMIN").length ?? 0;
  const pmCount = users?.filter((u) => u.role === "PROJECT_MANAGER").length ?? 0;

  return (
    <div>
      <div className="page-header">
        <div>
          <h1>Admin</h1>
          <p>Manage who has Project Manager and Admin access.</p>
        </div>
      </div>

      {error && <div className="inline-error">{error}</div>}

      {users && (
        <div className="stat-grid">
          <div className="stat-card">
            <div>
              <div className="stat-value">{users.length}</div>
              <div className="stat-label">Total Users</div>
            </div>
            <div className="stat-card-icon">
              <Users size={18} strokeWidth={2.2} />
            </div>
          </div>
          <div className="stat-card">
            <div>
              <div className="stat-value">{pmCount}</div>
              <div className="stat-label">Project Managers</div>
            </div>
            <div className="stat-card-icon">
              <Briefcase size={18} strokeWidth={2.2} />
            </div>
          </div>
          <div className="stat-card">
            <div>
              <div className="stat-value">{adminCount}</div>
              <div className="stat-label">Admins</div>
            </div>
            <div className="stat-card-icon">
              <ShieldCheck size={18} strokeWidth={2.2} />
            </div>
          </div>
        </div>
      )}

      {users === null ? (
        <div className="card">Loading...</div>
      ) : (
        <table className="table">
          <thead>
            <tr>
              <th>User</th>
              <th>Email</th>
              <th>Joined</th>
              <th>Role</th>
              <th></th>
            </tr>
          </thead>
          <tbody>
            {users.map((u) => {
              const isSelf = u.id === me?.id;
              return (
                <tr key={u.id}>
                  <td>
                    <div style={{ display: "flex", alignItems: "center", gap: 10 }}>
                      <Avatar user={u} style={{ marginLeft: 0 }} />
                      <span>
                        {u.firstName} {u.lastName}
                        {isSelf && <span style={{ color: "var(--text-muted)" }}> (you)</span>}
                      </span>
                    </div>
                  </td>
                  <td>{u.email}</td>
                  <td>{new Date(u.createdAt).toLocaleDateString()}</td>
                  <td>
                    <select
                      value={u.role}
                      disabled={isSelf || savingId === u.id}
                      onChange={(e) => handleRoleChange(u, e.target.value as Role)}
                      style={{
                        padding: "6px 10px",
                        borderRadius: "var(--radius-sm)",
                        border: "1px solid var(--border-strong)",
                        background: "var(--surface)",
                        color: "var(--text)",
                        fontSize: 13,
                        fontWeight: 600,
                      }}
                    >
                      {ROLE_OPTIONS.map((role) => (
                        <option key={role} value={role}>
                          {ROLE_OPTION_LABEL[role]}
                        </option>
                      ))}
                    </select>
                  </td>
                  <td>
                    <div style={{ display: "flex", gap: 6 }}>
                      <button
                        className="btn btn-secondary"
                        onClick={() => setEditingUser(u)}
                        disabled={savingId === u.id}
                        aria-label="Edit user"
                      >
                        <Pencil size={14} strokeWidth={2.2} />
                      </button>
                      <button
                        className="btn btn-danger"
                        onClick={() => handleDelete(u)}
                        disabled={isSelf || savingId === u.id}
                        aria-label="Delete user"
                      >
                        <Trash2 size={14} strokeWidth={2.2} />
                      </button>
                    </div>
                  </td>
                </tr>
              );
            })}
          </tbody>
        </table>
      )}

      {editingUser && (
        <EditUserModal
          user={editingUser}
          onClose={() => setEditingUser(null)}
          onSaved={(updated) => {
            setUsers((prev) => prev!.map((u) => (u.id === updated.id ? updated : u)));
            setEditingUser(null);
            showToast(`${updated.firstName} ${updated.lastName} updated`);
          }}
        />
      )}
    </div>
  );
}

function EditUserModal({
  user,
  onClose,
  onSaved,
}: {
  user: User;
  onClose: () => void;
  onSaved: (updated: User) => void;
}) {
  const [firstName, setFirstName] = useState(user.firstName);
  const [lastName, setLastName] = useState(user.lastName);
  const [email, setEmail] = useState(user.email);
  const [password, setPassword] = useState("");
  const [error, setError] = useState<string | null>(null);
  const [isSubmitting, setIsSubmitting] = useState(false);

  async function handleSubmit(e: FormEvent) {
    e.preventDefault();
    setError(null);
    setIsSubmitting(true);
    try {
      const updated = await userService.updateUser(user.id, {
        firstName,
        lastName,
        email,
        password: password || undefined,
      });
      onSaved(updated);
    } catch (err) {
      setError(getErrorMessage(err));
    } finally {
      setIsSubmitting(false);
    }
  }

  return (
    <Modal title={`Edit ${user.firstName} ${user.lastName}`} onClose={onClose}>
      {error && <div className="form-banner-error">{error}</div>}
      <form onSubmit={handleSubmit}>
        <div className="form-group">
          <label htmlFor="editFirstName">First name</label>
          <input id="editFirstName" value={firstName} onChange={(e) => setFirstName(e.target.value)} required />
        </div>
        <div className="form-group">
          <label htmlFor="editLastName">Last name</label>
          <input id="editLastName" value={lastName} onChange={(e) => setLastName(e.target.value)} required />
        </div>
        <div className="form-group">
          <label htmlFor="editEmail">Email</label>
          <input
            id="editEmail"
            type="email"
            value={email}
            onChange={(e) => setEmail(e.target.value)}
            required
          />
        </div>
        <div className="form-group">
          <label htmlFor="editPassword">New password</label>
          <input
            id="editPassword"
            type="password"
            placeholder="Leave blank to keep current password"
            value={password}
            onChange={(e) => setPassword(e.target.value)}
          />
        </div>
        <div className="modal-actions">
          <button type="button" className="btn btn-secondary" onClick={onClose}>
            Cancel
          </button>
          <button type="submit" className="btn btn-primary" disabled={isSubmitting}>
            {isSubmitting ? "Saving..." : "Save changes"}
          </button>
        </div>
      </form>
    </Modal>
  );
}
