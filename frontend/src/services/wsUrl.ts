export function getWebSocketUrl(): string {
  const apiBase = import.meta.env.VITE_API_URL ?? "http://localhost:8080/api";
  return apiBase.replace(/\/api$/, "").replace(/^http/, "ws") + "/ws";
}
