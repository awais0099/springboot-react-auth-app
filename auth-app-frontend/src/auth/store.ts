import { create } from "zustand";
import { authService } from "@/api/authService";

interface User {
  name: string;
  email: string;
}

interface AuthState {
  user: User | null;
  accessToken: string | null;
  isAuthenticated: boolean;
  isHydrated: boolean;
  login: (user: User, token: string) => void;
  logout: () => void;
  updateAccessToken: (token: string) => void;
  initializeAuth: () => Promise<void>;
}

export const useAuthStore = create<AuthState>((set) => ({
  user: null,
  accessToken: null,
  isAuthenticated: false,
  isHydrated: false, // Remains false until validation loop settles

  login: (user, token) => {
    localStorage.setItem("accessToken", token);
    localStorage.setItem("userEmail", user.email); // Cache the email identifier safely
    set({ user, accessToken: token, isAuthenticated: true });
  },

  logout: () => {
    localStorage.removeItem("accessToken");
    localStorage.removeItem("userEmail");
    set({ user: null, accessToken: null, isAuthenticated: false });
  },

  initializeAuth: async () => {
    const cachedToken = localStorage.getItem("accessToken");
    const cachedEmail = localStorage.getItem("userEmail");

    // Scenario A: No session signature exists. Gracefully boot as public guest.
    if (!cachedToken || !cachedEmail) {
      set({ isHydrated: true, isAuthenticated: false });
      return;
    }

    try {
      // Scenario B: Session token found. Verify validity against Spring Security Filter Chain
      // Pass the cached token in memory first so your Axios request interceptor can use it
      set({ accessToken: cachedToken });
      
      const userData = await authService.testInterceptor(cachedEmail);
      
      // Verification Success: Safe to mount protected layout spaces
      set({
        user: userData,
        isAuthenticated: true,
        isHydrated: true
      });
    } catch (error) {
      // Scenario C: Token is expired, missing, or malformed. Purge invalid session data silently.
      console.error("Session integrity verification failed. Clearing credentials.");
      localStorage.removeItem("accessToken");
      localStorage.removeItem("userEmail");
      set({
        user: null,
        accessToken: null,
        isAuthenticated: false,
        isHydrated: true // Release the mounting lock so App.tsx can show the Login view
      });
    }
  },

  updateAccessToken: (token) => {
    localStorage.setItem("accessToken", token);
    set({ accessToken: token });
  },
}));