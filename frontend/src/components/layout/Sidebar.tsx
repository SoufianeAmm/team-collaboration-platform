import { NavLink } from "react-router-dom";
import { LayoutDashboard, FolderKanban, Bell, UserRound, ShieldCheck, Boxes } from "lucide-react";
import { useAuth } from "../../context/AuthContext";

const navItems = [
  { to: "/dashboard", label: "Dashboard", icon: LayoutDashboard },
  { to: "/projects", label: "Projects", icon: FolderKanban },
  { to: "/notifications", label: "Notifications", icon: Bell },
  { to: "/profile", label: "Profile", icon: UserRound },
];

export function Sidebar() {
  const { user } = useAuth();

  return (
    <aside className="sidebar">
      <div className="sidebar-brand">
        <div className="sidebar-brand-mark">
          <Boxes size={17} strokeWidth={2.4} />
        </div>
        <span className="sidebar-brand-text">TeamCollab</span>
      </div>
      <nav className="sidebar-nav">
        {navItems.map((item) => (
          <NavLink
            key={item.to}
            to={item.to}
            className={({ isActive }) => `sidebar-link${isActive ? " active" : ""}`}
          >
            <item.icon size={17} strokeWidth={2.2} />
            {item.label}
          </NavLink>
        ))}
        {user?.role === "ADMIN" && (
          <NavLink to="/admin" className={({ isActive }) => `sidebar-link${isActive ? " active" : ""}`}>
            <ShieldCheck size={17} strokeWidth={2.2} />
            Admin
          </NavLink>
        )}
      </nav>
    </aside>
  );
}
