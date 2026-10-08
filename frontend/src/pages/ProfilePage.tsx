import { useRef, useState } from "react";
import type { ChangeEvent, FormEvent } from "react";
import { Camera, X } from "lucide-react";
import { Avatar } from "../components/Avatar";
import { useAuth } from "../context/AuthContext";
import { useToast } from "../context/ToastContext";
import { getErrorMessage } from "../services/api";
import * as userService from "../services/userService";

export function ProfilePage() {
  const { user, updateUser } = useAuth();
  const showToast = useToast();
  const fileInputRef = useRef<HTMLInputElement>(null);

  const [firstName, setFirstName] = useState(user?.firstName ?? "");
  const [lastName, setLastName] = useState(user?.lastName ?? "");
  const [isSavingName, setIsSavingName] = useState(false);
  const [isSavingPhoto, setIsSavingPhoto] = useState(false);

  if (!user) return null;

  const nameChanged = firstName.trim() !== user.firstName || lastName.trim() !== user.lastName;

  async function handleSaveName(e: FormEvent) {
    e.preventDefault();
    if (!firstName.trim() || !lastName.trim()) return;
    setIsSavingName(true);
    try {
      const updated = await userService.updateProfile(firstName.trim(), lastName.trim());
      updateUser(updated);
      showToast("Profile updated");
    } catch (err) {
      showToast(getErrorMessage(err), "error");
    } finally {
      setIsSavingName(false);
    }
  }

  async function handlePhotoSelected(e: ChangeEvent<HTMLInputElement>) {
    const file = e.target.files?.[0];
    if (!file) return;
    setIsSavingPhoto(true);
    try {
      const updated = await userService.uploadAvatar(file);
      updateUser(updated);
      showToast("Profile photo updated");
    } catch (err) {
      showToast(getErrorMessage(err), "error");
    } finally {
      setIsSavingPhoto(false);
      if (fileInputRef.current) fileInputRef.current.value = "";
    }
  }

  async function handleRemovePhoto() {
    setIsSavingPhoto(true);
    try {
      const updated = await userService.removeAvatar();
      updateUser(updated);
      showToast("Profile photo removed");
    } catch (err) {
      showToast(getErrorMessage(err), "error");
    } finally {
      setIsSavingPhoto(false);
    }
  }

  return (
    <div>
      <div className="page-header">
        <h1>Profile</h1>
      </div>

      <div className="card" style={{ maxWidth: 480 }}>
        <div style={{ display: "flex", alignItems: "center", gap: 18, marginBottom: 24 }}>
          <div className="avatar-edit">
            <Avatar user={user} className="topbar-avatar" size={64} />
            <button
              className="avatar-edit-btn"
              onClick={() => fileInputRef.current?.click()}
              disabled={isSavingPhoto}
              aria-label="Change photo"
              type="button"
            >
              <Camera size={13} strokeWidth={2.4} />
            </button>
          </div>
          <div>
            <h2>{user.firstName} {user.lastName}</h2>
            <span className="badge badge-accent" style={{ marginTop: 4 }}>{user.role}</span>
          </div>
        </div>

        <input
          ref={fileInputRef}
          type="file"
          accept=".png,.jpg,.jpeg,.gif,.webp"
          onChange={handlePhotoSelected}
          style={{ display: "none" }}
        />

        {user.profilePictureUrl && (
          <button
            className="btn btn-danger"
            onClick={handleRemovePhoto}
            disabled={isSavingPhoto}
            style={{ marginBottom: 20 }}
          >
            <X size={14} strokeWidth={2.2} />
            Remove photo
          </button>
        )}

        <form onSubmit={handleSaveName}>
          <div className="form-group">
            <label htmlFor="firstName">First name</label>
            <input id="firstName" value={firstName} onChange={(e) => setFirstName(e.target.value)} required />
          </div>
          <div className="form-group">
            <label htmlFor="lastName">Last name</label>
            <input id="lastName" value={lastName} onChange={(e) => setLastName(e.target.value)} required />
          </div>
          <div className="form-group">
            <label>Username</label>
            <input value={user.username} disabled />
          </div>
          <div className="form-group">
            <label>Email</label>
            <input value={user.email} disabled />
          </div>

          <button type="submit" className="btn btn-primary" disabled={!nameChanged || isSavingName}>
            {isSavingName ? "Saving..." : "Save changes"}
          </button>
        </form>
      </div>
    </div>
  );
}
