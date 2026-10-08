import { useEffect, useState } from "react";
import { Users, ShieldCheck, Briefcase } from "lucide-react";
import { Avatar } from "../components/Avatar";
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

  useEffect(() => {
    userService
      .listUsers()
      .then(setUsers)
      .catch((err) => setError(getErrorMessage(err)));
  }, []);

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
                </tr>
              );
            })}
          </tbody>
        </table>
      )}
    </div>
  );
}
