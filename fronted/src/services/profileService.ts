import type { UserProfile } from "../types";

interface ApiResponse<T> {
  success: boolean;
  message: string;
  data: T;
}

interface UserDto {
  id: string;
  universityEmail: string;
  displayName: string;
  bio: string | null;
  profileImageUrl: string | null;
  role: string;
  trustLevel: string;
  createdAt: string;
}

function mapUserDto(dto: UserDto): UserProfile {
  return {
    id: dto.id,
    displayName: dto.displayName,
    universityEmail: dto.universityEmail,
    bio: dto.bio ?? "",
    profileImageUrl: dto.profileImageUrl ?? undefined,
    role: dto.role,
    trustLevel: dto.trustLevel,
    createdAt: dto.createdAt,
  };
}

export async function fetchProfile(token: string): Promise<UserProfile> {
  const res = await fetch("/api/auth/me", {
    method: "GET",
    headers: { Authorization: `Bearer ${token}` },
  });
  const body: ApiResponse<UserDto> = await res.json().catch(() => ({ success: false, message: "Parse error" }));
  if (!res.ok || !body.data) throw new Error(body.message || "Profile konnte nicht geladen werden");
  return mapUserDto(body.data);
}

export async function updateProfile(
  token: string,
  updates: Partial<UserProfile> & { profileImage?: File | null },
): Promise<UserProfile> {
  const form = new FormData();
  if (updates.displayName !== undefined) form.append("displayName", updates.displayName);
  if (updates.bio !== undefined) form.append("bio", updates.bio);
  if (updates.profileImage) form.append("profileImage", updates.profileImage);

  const res = await fetch("/api/auth/me", {
    method: "PUT",
    headers: { Authorization: `Bearer ${token}` },
    body: form,
  });
  const body: ApiResponse<UserDto> = await res.json().catch(() => ({ success: false, message: "Parse error" }));
  if (!res.ok || !body.data) throw new Error(body.message || "Profil konnte nicht aktualisiert werden");
  return mapUserDto(body.data);
}
