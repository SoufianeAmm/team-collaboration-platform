import { useNavigate } from "react-router-dom";
import { LogOut } from "lucide-react";
import { Avatar } from "../Avatar";
import { useAuth } from "../../context/AuthContext";
import { NotificationBell } from "./NotificationBell";

export function Topbar() {
  const { user, logout } = useAuth();
  const navigate = useNavigate();

  function handleLogout() {
    logout();
    navigate("/login");
  }

  return (
    <header className="topbar">
      <NotificationBell />
      <div className="topbar-user">
        {user && <Avatar user={user} className="topbar-avatar" />}
        <span>{user?.firstName} {user?.lastName}</span>
      </div>
      <button className="btn btn-secondary" onClick={handleLogout}>
        <LogOut size={15} strokeWidth={2.2} />
        Log out
      </button>
    </header>
  );
}
