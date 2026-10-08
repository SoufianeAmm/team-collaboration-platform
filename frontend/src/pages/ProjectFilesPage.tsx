import { useEffect, useRef, useState } from "react";
import type { ChangeEvent } from "react";
import { Paperclip, Upload, Download, Trash2 } from "lucide-react";
import { EmptyState } from "../components/EmptyState";
import { useAuth } from "../context/AuthContext";
import { useToast } from "../context/ToastContext";
import { getErrorMessage } from "../services/api";
import * as fileService from "../services/fileService";
import type { FileUpload } from "../types/file";
import { useProjectContext } from "./ProjectLayout";

function formatSize(bytes: number): string {
  if (bytes < 1024) return `${bytes} B`;
  if (bytes < 1024 * 1024) return `${(bytes / 1024).toFixed(1)} KB`;
  return `${(bytes / 1024 / 1024).toFixed(1)} MB`;
}

export function ProjectFilesPage() {
  const { project } = useProjectContext();
  const { user } = useAuth();
  const showToast = useToast();
  const fileInputRef = useRef<HTMLInputElement>(null);
  const [files, setFiles] = useState<FileUpload[] | null>(null);
  const [error, setError] = useState<string | null>(null);
  const [isUploading, setIsUploading] = useState(false);

  function load() {
    fileService
      .listFiles(project.id)
      .then(setFiles)
      .catch((err) => setError(getErrorMessage(err)));
  }

  useEffect(load, [project.id]);

  async function handleFileSelected(e: ChangeEvent<HTMLInputElement>) {
    const file = e.target.files?.[0];
    if (!file) return;
    setIsUploading(true);
    try {
      const uploaded = await fileService.uploadFile(project.id, file);
      setFiles((prev) => [...(prev ?? []), uploaded]);
      showToast(`"${file.name}" uploaded`);
    } catch (err) {
      showToast(getErrorMessage(err), "error");
    } finally {
      setIsUploading(false);
      if (fileInputRef.current) fileInputRef.current.value = "";
    }
  }

  async function handleDownload(file: FileUpload) {
    try {
      await fileService.downloadFile(file.id, file.originalFilename);
    } catch (err) {
      showToast(getErrorMessage(err), "error");
    }
  }

  async function handleDelete(file: FileUpload) {
    if (!confirm(`Delete "${file.originalFilename}"?`)) return;
    try {
      await fileService.deleteFile(file.id);
      setFiles((prev) => prev!.filter((f) => f.id !== file.id));
      showToast("File deleted");
    } catch (err) {
      showToast(getErrorMessage(err), "error");
    }
  }

  function canDelete(file: FileUpload): boolean {
    return user?.id === file.uploader.id || user?.id === project.owner.id || user?.role === "ADMIN";
  }

  return (
    <div>
      <div className="page-header">
        <div>
          <p>PDF, PNG, JPG, DOCX, or ZIP — up to 10 MB.</p>
        </div>
        <div>
          <input
            ref={fileInputRef}
            type="file"
            accept=".pdf,.png,.jpg,.jpeg,.docx,.zip"
            onChange={handleFileSelected}
            style={{ display: "none" }}
          />
          <button
            className="btn btn-primary"
            onClick={() => fileInputRef.current?.click()}
            disabled={isUploading}
          >
            <Upload size={15} strokeWidth={2.2} />
            {isUploading ? "Uploading..." : "Upload File"}
          </button>
        </div>
      </div>

      {error && <div className="inline-error">{error}</div>}

      {files === null ? (
        <div className="card">Loading...</div>
      ) : files.length === 0 ? (
        <div className="card">
          <EmptyState icon={<Paperclip size={24} />} title="No files yet" description="Upload the first file to share it with the team." />
        </div>
      ) : (
        <table className="table">
          <thead>
            <tr>
              <th>Name</th>
              <th>Size</th>
              <th>Uploaded by</th>
              <th>Date</th>
              <th></th>
            </tr>
          </thead>
          <tbody>
            {files.map((file) => (
              <tr key={file.id}>
                <td>{file.originalFilename}</td>
                <td>{formatSize(file.fileSize)}</td>
                <td>{file.uploader.firstName} {file.uploader.lastName}</td>
                <td>{new Date(file.uploadDate).toLocaleDateString()}</td>
                <td style={{ display: "flex", gap: 8, justifyContent: "flex-end" }}>
                  <button className="btn btn-secondary" onClick={() => handleDownload(file)}>
                    <Download size={14} strokeWidth={2.2} />
                    Download
                  </button>
                  {canDelete(file) && (
                    <button className="btn btn-danger" onClick={() => handleDelete(file)}>
                      <Trash2 size={14} strokeWidth={2.2} />
                      Delete
                    </button>
                  )}
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      )}
    </div>
  );
}
