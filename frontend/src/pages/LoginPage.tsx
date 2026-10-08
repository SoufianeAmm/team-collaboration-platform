import { useState } from "react";
import type { FormEvent } from "react";
import { Link, useNavigate } from "react-router-dom";
import { Boxes, KanbanSquare, MessagesSquare, BellRing } from "lucide-react";
import { useAuth } from "../context/AuthContext";
import { getErrorMessage } from "../services/api";

export function LoginPage() {
  const { login } = useAuth();
  const navigate = useNavigate();
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [error, setError] = useState<string | null>(null);
  const [isSubmitting, setIsSubmitting] = useState(false);

  async function handleSubmit(e: FormEvent) {
    e.preventDefault();
    setError(null);
    setIsSubmitting(true);
    try {
      await login({ email, password });
      navigate("/dashboard");
    } catch (err) {
      setError(getErrorMessage(err));
    } finally {
      setIsSubmitting(false);
    }
  }

  return (
    <div className="auth-page">
      <div className="auth-showcase">
        <div className="auth-showcase-brand">
          <div className="auth-showcase-mark">
            <Boxes size={18} strokeWidth={2.4} />
          </div>
          TeamCollab
        </div>

        <div className="auth-showcase-body">
          <h2>Where your team actually gets things done.</h2>
          <p>Projects, tasks, chat, and files — kept in sync for everyone, in real time.</p>
        </div>

        <div className="auth-showcase-points">
          <div className="auth-showcase-point">
            <KanbanSquare size={14} strokeWidth={2.2} />
            Kanban boards that update live across your team
          </div>
          <div className="auth-showcase-point">
            <MessagesSquare size={14} strokeWidth={2.2} />
            Built-in chat and comments, right next to the work
          </div>
          <div className="auth-showcase-point">
            <BellRing size={14} strokeWidth={2.2} />
            Instant notifications so nothing slips through
          </div>
        </div>
      </div>

      <div className="auth-form-pane">
        <div className="auth-card">
          <div className="auth-card-brand">
            <div className="auth-card-brand-mark">
              <Boxes size={15} strokeWidth={2.4} />
            </div>
            TeamCollab
          </div>

          <h1>Glad to see you again</h1>
          <p className="auth-subtitle">Log in to your workspace</p>

          {error && <div className="form-banner-error">{error}</div>}

          <form onSubmit={handleSubmit}>
            <div className="form-group">
              <label htmlFor="email">Email</label>
              <input
                id="email"
                type="email"
                value={email}
                onChange={(e) => setEmail(e.target.value)}
                required
              />
            </div>
            <div className="form-group">
              <label htmlFor="password">Password</label>
              <input
                id="password"
                type="password"
                value={password}
                onChange={(e) => setPassword(e.target.value)}
                required
              />
            </div>
            <button type="submit" className="btn btn-primary" disabled={isSubmitting}>
              {isSubmitting ? "Logging in..." : "Log in"}
            </button>
          </form>

          <div className="auth-switch">
            Don't have an account? <Link to="/register">Sign up</Link>
          </div>
        </div>
      </div>
    </div>
  );
}
