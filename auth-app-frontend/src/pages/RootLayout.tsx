import React from "react";
import { Outlet } from "react-router";
import AppNavbar from "@/components/layout/AppNavbar";

export default function RootLayout() {
  return (
    <div className="min-h-screen bg-slate-950 flex flex-col">
      {/* Renders statically at the absolute crest of the application wrapper */}
      <AppNavbar />
      
      {/* Main viewport injection vector */}
      <main className="flex-1">
        <Outlet />
      </main>
    </div>
  );
}