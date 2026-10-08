import type { CSSProperties } from "react";
import { toAbsoluteAssetUrl } from "../services/assetUrl";

interface AvatarUser {
  firstName: string;
  lastName: string;
  profilePictureUrl?: string | null;
}

export function Avatar({
  user,
  size,
  className = "avatar",
  style: styleOverride,
}: {
  user: AvatarUser;
  size?: number;
  className?: string;
  style?: CSSProperties;
}) {
  const initials = `${user.firstName[0] ?? ""}${user.lastName[0] ?? ""}`.toUpperCase();
  const src = toAbsoluteAssetUrl(user.profilePictureUrl);
  const sizeStyle: CSSProperties | undefined = size
    ? { width: size, height: size, fontSize: Math.round(size * 0.38) }
    : undefined;
  const style = { ...sizeStyle, ...styleOverride };

  if (src) {
    return <img className={className} src={src} style={style} alt={`${user.firstName} ${user.lastName}`} />;
  }
  return (
    <span className={className} style={style}>
      {initials}
    </span>
  );
}
