/** Mirrors the backend's GlobalExceptionHandler error shape - every failed request comes back like this. */
export interface ApiErrorResponse {
  timestamp: string;
  status: number;
  error: string;
  message: string;
  fieldErrors: Record<string, string> | null;
}
