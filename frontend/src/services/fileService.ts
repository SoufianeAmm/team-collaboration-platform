import { api } from "./api";
import type { FileUpload } from "../types/file";

export async function listFiles(projectId: number): Promise<FileUpload[]> {
  const { data } = await api.get<FileUpload[]>(`/projects/${projectId}/files`);
  return data;
}

export async function uploadFile(projectId: number, file: File, taskId?: number): Promise<FileUpload> {
  const formData = new FormData();
  formData.append("file", file);
  const params = taskId ? { taskId } : undefined;
  // Don't set Content-Type manually - axios/the browser need to add the multipart boundary themselves.
  const { data } = await api.post<FileUpload>(`/projects/${projectId}/files`, formData, { params });
  return data;
}

export async function deleteFile(fileId: number): Promise<void> {
  await api.delete(`/files/${fileId}`);
}

/** Triggers a real browser "Save As" download - a plain <a href> can't carry the JWT, so we fetch
 * the bytes ourselves (authenticated) and hand the browser a local blob URL to save instead. */
export async function downloadFile(fileId: number, filename: string): Promise<void> {
  const response = await api.get(`/files/${fileId}/download`, { responseType: "blob" });
  const url = window.URL.createObjectURL(response.data);
  const link = document.createElement("a");
  link.href = url;
  link.download = filename;
  document.body.appendChild(link);
  link.click();
  link.remove();
  window.URL.revokeObjectURL(url);
}
