import type { User } from "./user";

export interface FileUpload {
  id: number;
  originalFilename: string;
  fileType: string;
  fileSize: number;
  uploadDate: string;
  uploader: User;
  projectId: number;
  taskId: number | null;
}
