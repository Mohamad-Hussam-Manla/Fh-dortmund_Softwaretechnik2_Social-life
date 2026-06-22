import { create } from "zustand";
import type { UserProfile, AuthState } from "../types";

const STORAGE_KEY_TOKEN = "token";
const STORAGE_KEY_USER = "userProfile";

function loadFromStorage(): { token: string | null; user: UserProfile | null } {
  const token = localStorage.getItem(STORAGE_KEY_TOKEN);
  const rawUser = localStorage.getItem(STORAGE_KEY_USER);
  let user: UserProfile | null = null;
  if (rawUser) {
    try {
      user = JSON.parse(rawUser);
    } catch {
      /* corrupt — ignore */
    }
  }
  return { token, user };
}

export const useAuthStore = create<AuthState>((set) => {
  const initial = loadFromStorage();
  return {
    token: initial.token,
    user: initial.user,
    isAuthenticated: !!initial.token && !!initial.user,

    login: (token: string, user: UserProfile) => {
      localStorage.setItem(STORAGE_KEY_TOKEN, token);
      localStorage.setItem(STORAGE_KEY_USER, JSON.stringify(user));
      set({ token, user, isAuthenticated: true });
    },

    logout: () => {
      localStorage.removeItem(STORAGE_KEY_TOKEN);
      localStorage.removeItem(STORAGE_KEY_USER);
      set({ token: null, user: null, isAuthenticated: false });
    },

    updateProfile: (updates: Partial<UserProfile>) => {
      set((state) => {
        const nextUser = state.user ? { ...state.user, ...updates } : null;
        if (nextUser) {
          localStorage.setItem(STORAGE_KEY_USER, JSON.stringify(nextUser));
        }
        return { user: nextUser, isAuthenticated: !!nextUser };
      });
    },
  };
});
