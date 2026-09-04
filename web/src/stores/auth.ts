import { create } from 'zustand'
import { persist, createJSONStorage } from 'zustand/middleware'
import type { LoginData } from '@/api/types'

interface AuthState {
  token: string
  userId: string
  userName: string
  setAuth: (data: LoginData) => void
  clearAuth: () => void
}

export const useAuthStore = create<AuthState>()(
  persist(
    (set) => ({
      token: '',
      userId: '',
      userName: '',
      setAuth: (data) => set({ token: data.token, userId: data.user_id || '', userName: data.user_name || '' }),
      clearAuth: () => set({ token: '', userId: '', userName: '' }),
    }),
    { name: 'ai-capability-auth', storage: createJSONStorage(() => localStorage) },
  ),
)
