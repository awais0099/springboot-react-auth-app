import { useEffect } from "react";
import { BrowserRouter, Routes, Route, Navigate } from "react-router"; // or 'react-router-dom' depending on your package.json
import { useAuthStore } from "./auth/store";

// Layouts & Guards
import RootLayout from './pages/RootLayout.tsx'
import { ProtectedRoute } from "./components/guards/ProtectedRoute";

// Pages
import Home from './pages/Home.tsx'
import Login from './pages/Login.tsx'
import Signup from './pages/Signup.tsx'
import Dashboard from "./pages/Dashboard.tsx";

export default function App() {
  const initializeAuth = useAuthStore((state) => state.initializeAuth);
  const isHydrated = useAuthStore((state) => state.isHydrated);

  // Synchronize token state on startup
  useEffect(() => {
    initializeAuth();
  }, [initializeAuth]);

  // Prevent UI flashing while checking localStorage
  if (!isHydrated) {
    return (
      <div className="flex h-screen w-screen items-center justify-center bg-slate-950 text-slate-200">
        <div className="text-center space-y-2">
          <div className="h-8 w-8 animate-spin rounded-full border-4 border-emerald-500 border-t-transparent mx-auto" />
          <p className="text-sm text-slate-400 font-medium">Verifying session...</p>
        </div>
      </div>
    );
  }

  return (
    <BrowserRouter>
      <Routes>
        {/* 1. PUBLIC WEBSITE ROUTES (Uses your RootLayout navbar/footer) */}
        <Route path='/' element={<RootLayout />}>
          <Route index element={<Home />} />
        </Route>

        {/* 2. AUTHENTICATION ROUTES (Standalone full-screen, no RootLayout) */}
        <Route path="/login" element={<Login />} />
        <Route path="/signup" element={<Signup />} />

        {/* 3. SECURE APP ROUTES (Guarded via ProtectedRoute) */}
        <Route element={<ProtectedRoute />}>
          {/* If authenticated, /dashboard renders here. If not, drops to /login */}
          <Route path="/dashboard" element={<Dashboard />} />
        </Route>

        {/* Fallback Catch-All */}
        <Route path="*" element={<Navigate to="/" replace />} />
      </Routes>
    </BrowserRouter>
  );
}