export interface UserProfile {
  id?: string;
  displayName: string;
  universityEmail: string;
  bio?: string;
  profileImageUrl?: string;
  role?: string;
  trustLevel?: string;
  createdAt?: string;
  /** local-only, not stored on backend */
  hobbies?: string[];
  /** local-only, not stored on backend */
  likes?: string[];
}

export interface AuthState {
  token: string | null;
  user: UserProfile | null;
  isAuthenticated: boolean;
  login: (token: string, user: UserProfile) => void;
  logout: () => void;
  updateProfile: (updates: Partial<UserProfile>) => void;
}
