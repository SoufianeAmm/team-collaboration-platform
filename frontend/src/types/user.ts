export type Role = "USER" | "PROJECT_MANAGER" | "ADMIN";

export interface User {
  id: number;
  username: string;
  firstName: string;
  lastName: string;
  email: string;
  role: Role;
  profilePictureUrl: string | null;
  createdAt: string;
}
