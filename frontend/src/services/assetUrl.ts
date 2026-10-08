/** Turns a relative API path (e.g. "/api/avatars/xyz.png") into an absolute URL, so <img> tags
 * work regardless of what VITE_API_URL points at. Returns null through untouched. */
export function toAbsoluteAssetUrl(path: string | null | undefined): string | null {
  if (!path) return null;
  const apiBase = import.meta.env.VITE_API_URL ?? "http://localhost:8080/api";
  const origin = apiBase.replace(/\/api$/, "");
  return origin + path;
}
