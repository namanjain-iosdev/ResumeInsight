import { create } from 'zustand';
import { persist } from 'zustand/middleware';

const useAuthStore = create(
  persist(
    (set, get) => ({
      user: null,
      token: null,
      isAuthenticated: false,

      login: (userData, token) => set({
        user: userData,
        token,
        isAuthenticated: true,
      }),

      logout: () => set({
        user: null,
        token: null,
        isAuthenticated: false,
      }),

      updateUser: (userData) => set({ user: { ...get().user, ...userData } }),

      isAdmin: () => {
        const user = get().user;
        return user?.roles?.includes('ROLE_ADMIN') || false;
      },
    }),
    { name: 'auth-storage' }
  )
);

export default useAuthStore;
