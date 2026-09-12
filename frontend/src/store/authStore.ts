import { create } from 'zustand';
import { persist } from 'zustand/middleware';
import { STORAGE_KEYS } from '@/api/client';
import { authApi } from '@/api/services';
import type { LoginResponse, User } from '@/types';

interface AuthState {
  user: User | null;
  permissions: string[];
  roleGroups: string[];
  initialized: boolean;
  login: (username: string, password: string) => Promise<LoginResponse>;
  logout: () => Promise<void>;
  restoreSession: () => Promise<void>;
  hasPermission: (code: string) => boolean;
  hasAnyPermission: (codes: string[]) => boolean;
  hasRole: (roleGroupCode: string) => boolean;
}

export const useAuthStore = create<AuthState>()(
  persist(
    (set, get) => ({
      user: null,
      permissions: [],
      roleGroups: [],
      initialized: false,

      login: async (username, password) => {
        const result = await authApi.login(username, password);
        localStorage.setItem(STORAGE_KEYS.accessToken, result.accessToken);
        localStorage.setItem(STORAGE_KEYS.refreshToken, result.refreshToken);
        set({
          user: result.user,
          permissions: result.permissions,
          roleGroups: result.roleGroups,
          initialized: true,
        });
        return result;
      },

      logout: async () => {
        try {
          await authApi.logout();
        } finally {
          localStorage.removeItem(STORAGE_KEYS.accessToken);
          localStorage.removeItem(STORAGE_KEYS.refreshToken);
          set({ user: null, permissions: [], roleGroups: [], initialized: true });
        }
      },

      /**
       * Goi khi tai lai trang: neu token con hieu luc thi lay lai ho so nguoi dung.
       */
      restoreSession: async () => {
        const token = localStorage.getItem(STORAGE_KEYS.accessToken);
        if (!token) {
          set({ user: null, permissions: [], roleGroups: [], initialized: true });
          return;
        }
        try {
          const user = await authApi.me();
          set({ user, initialized: true });
        } catch {
          set({ user: null, permissions: [], roleGroups: [], initialized: true });
        }
      },

      hasPermission: (code) => get().permissions.includes(code),
      hasAnyPermission: (codes) => codes.some((code) => get().permissions.includes(code)),
      hasRole: (roleGroupCode) => get().roleGroups.includes(roleGroupCode),
    }),
    {
      name: 'delivery.auth',
      partialize: (state) => ({
        user: state.user,
        permissions: state.permissions,
        roleGroups: state.roleGroups,
      }),
    },
  ),
);
