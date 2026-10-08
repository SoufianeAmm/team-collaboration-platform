import { Link } from "react-router-dom";

export function NotFoundPage() {
  return (
    <div className="auth-page">
      <div style={{ textAlign: "center" }}>
        <h1>404</h1>
        <p style={{ margin: "8px 0 20px" }}>This page doesn't exist.</p>
        <Link to="/dashboard" className="btn btn-primary">Back to Dashboard</Link>
      </div>
    </div>
  );
}
