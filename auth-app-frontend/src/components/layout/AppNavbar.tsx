import React from "react";
import { Link, useNavigate } from "react-router";
import { useAuthStore } from "@/auth/store";
import { Button } from "@/components/ui/button";
import { ShieldCheckIcon, LogOutIcon, LayoutDashboardIcon } from "lucide-react";

export default function AppNavbar() {
  const isAuthenticated = useAuthStore((state) => state.isAuthenticated);
  const logoutAction = useAuthStore((state) => state.logout);
  const navigate = useNavigate();

  const handleLogoutClick = () => {
    logoutAction();
    navigate("/login");
  };

  return (
    <header className="sticky top-0 z-50 w-full border-b border-slate-800 bg-slate-950/80 backdrop-blur-md">
      <div className="mx-auto flex h-16 max-w-6xl items-center justify-between px-6 sm:px-10">
        
        {/* Brand Core Identity */}
        <Link to="/" className="flex items-center gap-2 font-bold text-white tracking-tight text-lg hover:opacity-90">
          <ShieldCheckIcon className="h-6 w-6 text-emerald-400" />
          <span>Presta<span className="text-emerald-400">Shark</span></span>
        </Link>

        {/* Action Blocks Swapped Dynamically via Zustand State Matrix */}
        <nav className="flex items-center gap-4">
          {isAuthenticated ? (
            <>
              {/* Authenticated Links */}
              <Link to="/dashboard">
                <Button variant="ghost" className="text-slate-300 hover:text-white gap-2 cursor-pointer text-sm">
                  <LayoutDashboardIcon className="h-4 w-4 text-slate-400" />
                  Dashboard
                </Button>
              </Link>
              <Button 
                variant="outline" 
                onClick={handleLogoutClick}
                className="border-slate-800 text-slate-300 hover:bg-slate-900 hover:text-white gap-2 cursor-pointer text-sm"
              >
                <LogOutIcon className="h-4 w-4 text-slate-400" />
                Logout
              </Button>
            </>
          ) : (
            <>
              {/* Anonymous Guest Links */}
              <Link to="/login">
                <Button variant="ghost" className="text-slate-300 hover:text-white cursor-pointer text-sm">
                  Sign In
                </Button>
              </Link>
              <Link to="/signup">
                <Button className="bg-emerald-600 hover:bg-emerald-500 text-white shadow-sm font-medium cursor-pointer text-sm">
                  Get Started
                </Button>
              </Link>
            </>
          )}
        </nav>

      </div>
    </header>
  );
}