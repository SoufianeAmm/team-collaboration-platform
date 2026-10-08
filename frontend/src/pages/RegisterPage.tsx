import { useState } from "react";
import type { FormEvent } from "react";
import { Link, useNavigate } from "react-router-dom";
import { Boxes, FolderKanban, Users, FileStack } from "lucide-react";
import { useAuth } from "../context/AuthContext";
import { getErrorMessage, getFieldErrors } from "../services/api";

export function RegisterPage() {
  const { register } = useAuth();
  const navigate = useNavigate();
  const [form, setForm] = useState({
    username: "",
    firstName: "",
    lastName: "",
    email: "",
    password: "",
  });
  const [error, setError] = useState<string | null>(null);
  const [fieldErrors, setFieldErrors] = useState<Record<string, string>>({});
  const [isSubmitting, setIsSubmitting] = useState(false);

  function update(field: keyof typeof form, value: string) {
    setForm((prev) => ({ ...prev, [field]: value }));
  }

  async function handleSubmit(e: FormEvent) {
    e.preventDefault();
    setError(null);
    setFieldErrors({});
    setIsSubmitting(true);
    try {
      await register(form);
      navigate("/dashboard");
    } catch (err) {
      setFieldErrors(getFieldErrors(err) ?? {});
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
          <h2>Bring your whole team into one workspace.</h2>
          <p>Set up projects, invite teammates, and start tracking work in a couple of minutes.</p>
        </div>

        <div className="auth-showcase-points">
          <div className="auth-showcase-point">
            <FolderKanban size={14} strokeWidth={2.2} />
            Organize work into projects and boards
          </div>
          <div className="auth-showcase-point">
            <Users size={14} strokeWidth={2.2} />
            Invite teammates and assign ownership
          </div>
          <div className="auth-showcase-point">
            <FileStack size={14} strokeWidth={2.2} />
            Share files and keep context in one place
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

        <h1>Create your account</h1>
        <p className="auth-subtitle">Start collaborating with your team</p>

        {error && <div className="form-banner-error">{error}</div>}

        <form onSubmit={handleSubmit}>
          <div className="form-group">
            <label htmlFor="firstName">First name</label>
            <input
              id="firstName"
              value={form.firstName}
              onChange={(e) => update("firstName", e.target.value)}
              required
            />
            {fieldErrors.firstName && <div className="form-error">{fieldErrors.firstName}</div>}
          </div>
          <div className="form-group">
            <label htmlFor="lastName">Last name</label>
            <input
              id="lastName"
              value={form.lastName}
              onChange={(e) => update("lastName", e.target.value)}
              required
            />
            {fieldErrors.lastName && <div className="form-error">{fieldErrors.lastName}</div>}
          </div>
          <div className="form-group">
            <label htmlFor="username">Username</label>
            <input
              id="username"
              value={form.username}
              onChange={(e) => update("username", e.target.value)}
              required
            />
            {fieldErrors.username && <div className="form-error">{fieldErrors.username}</div>}
          </div>
          <div className="form-group">
            <label htmlFor="email">Email</label>
            <input
              id="email"
              type="email"
              value={form.email}
              onChange={(e) => update("email", e.target.value)}
              required
            />
            {fieldErrors.email && <div className="form-error">{fieldErrors.email}</div>}
          </div>
          <div className="form-group">
            <label htmlFor="password">Password</label>
            <input
              id="password"
              type="password"
              value={form.password}
              onChange={(e) => update("password", e.target.value)}
              required
            />
            {fieldErrors.password && <div className="form-error">{fieldErrors.password}</div>}
          </div>
          <button type="submit" className="btn btn-primary" disabled={isSubmitting}>
            {isSubmitting ? "Creating account..." : "Create account"}
          </button>
        </form>

        <div className="auth-switch">
          Already have an account? <Link to="/login">Log in</Link>
        </div>
      </div>
      </div>
    </div>
  );
}
