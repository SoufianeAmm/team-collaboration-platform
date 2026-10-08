import axios from "axios";
import type { ApiErrorResponse } from "../types/api";

// Falls back to localhost so the app works even without a .env file copied in for local dev.
const baseURL = import.meta.env.VITE_API_URL ?? "http://localhost:8080/api";

export const api = axios.create({ baseURL });

// Every request automatically carries the JWT, if we have one - callers never set this header themselves.
api.interceptors.request.use((config) => {
  const token = localStorage.getItem("token");
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

// A 401 means our token is missing/expired - there's no refresh flow (Phase 4 kept JWT simple),
// so the only correct move is to drop it and send the user back to login.
api.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response?.status === 401) {
      localStorage.removeItem("token");
      if (window.location.pathname !== "/login") {
        window.location.href = "/login";
      }
    }
    return Promise.reject(error);
  }
);

/** Pulls the backend's error message out of an axios error - falls back for network-level failures. */
export function getErrorMessage(error: unknown): string {
  if (axios.isAxiosError<ApiErrorResponse>(error)) {
    return error.response?.data?.message ?? "Something went wrong. Please try again.";
  }
  return "Something went wrong. Please try again.";
}

/** For 400s from @Valid - the backend returns per-field messages, e.g. {"email": "must be well-formed"}. */
export function getFieldErrors(error: unknown): Record<string, string> | null {
  if (axios.isAxiosError<ApiErrorResponse>(error)) {
    return error.response?.data?.fieldErrors ?? null;
  }
  return null;
}
