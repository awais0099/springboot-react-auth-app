import React, { useState } from "react";
import { useAuthStore } from "@/auth/store";
import { authService } from "@/api/authService";
import { Button } from "@/components/ui/button";
import {
  Card,
  CardContent,
  CardDescription,
  CardHeader,
  CardTitle,
} from "@/components/ui/card";
import { ShieldCheckIcon, CpuIcon, RefreshCwIcon, LogOutIcon } from "lucide-react";

export default function Dashboard() {
  const user = useAuthStore((state) => state.user);
  const logoutAction = useAuthStore((state) => state.logout);
  const [isTestingApi, setIsTestingApi] = useState(false);
  const [apiResponse, setApiResponse] = useState<string | null>(null);

  // Safeguard: Ensure user exists before trying to destructure properties
  if (!user) return null;

  const handleTestSecureEndpoint = async () => {
    setIsTestingApi(true);
    setApiResponse(null);

    try {
      // Fires an authenticated call using the unified apiClient instance
      const data = await authService.testInterceptor(user.email);
      setApiResponse(`Success! Server responded with: ${JSON.stringify(data)}`);
    } catch (error: any) {
      // Captures the uniform error message structured by your response interceptor
      setApiResponse(`Interceptor Caught Error: ${error.message}`);
    } finally {
      setIsTestingApi(false);
    }
  };

  return (
    <div className="min-h-[calc(100vh-4rem)] bg-slate-950 text-slate-100 p-6 sm:p-10">
      <div className="mx-auto max-w-6xl space-y-8">
        
        {/* Header Section */}
        <div className="flex flex-col gap-4 sm:flex-row sm:items-center sm:justify-between border-b border-slate-800 pb-6">
          <div>
            <h1 className="text-3xl font-bold tracking-tight text-white">
              Control Panel
            </h1>
            <p className="text-sm text-slate-400">
              Welcome back, <span className="text-emerald-400 font-semibold">{user.name}</span> ({user.email})
            </p>
          </div>
          <Button 
            variant="destructive" 
            onClick={logoutAction}
            className="w-full sm:w-auto gap-2 cursor-pointer"
          >
            <LogOutIcon className="h-4 w-4" />
            Sign Out Session
          </Button>
        </div>

        {/* Dashboard Grid System */}
        <div className="grid gap-6 md:grid-cols-2">
          
          {/* Card 1: System Telemetry */}
          <Card className="bg-slate-900 border-slate-800 text-slate-100">
            <CardHeader className="flex flex-row items-center gap-4 space-y-0">
              <div className="rounded-lg bg-emerald-500/10 p-2 text-emerald-400">
                <ShieldCheckIcon className="h-6 w-6" />
              </div>
              <div>
                <CardTitle className="text-lg font-semibold text-white">Security Context</CardTitle>
                <CardDescription className="text-slate-400">Current authentication runtime data</CardDescription>
              </div>
            </CardHeader>
            <CardContent className="space-y-3 pt-2 text-sm text-slate-300">
              <div className="flex justify-between border-b border-slate-800 pb-2">
                <span className="text-slate-400">Session Status</span>
                <span className="font-mono text-xs text-emerald-400 bg-emerald-500/10 px-2 py-0.5 rounded">Active</span>
              </div>
              <div className="flex justify-between border-b border-slate-800 pb-2">
                <span className="text-slate-400">Identity Guard Token</span>
                <span className="font-mono text-xs text-slate-400">JWT Bearer Active</span>
              </div>
              <div className="flex justify-between">
                <span className="text-slate-400">Authorization Logic</span>
                <span className="font-mono text-xs text-slate-400">Spring Security Filtered</span>
              </div>
            </CardContent>
          </Card>

          {/* Card 2: Interceptor Testing Lab */}
          <Card className="bg-slate-900 border-slate-800 text-slate-100">
            <CardHeader className="flex flex-row items-center gap-4 space-y-0">
              <div className="rounded-lg bg-blue-500/10 p-2 text-blue-400">
                <CpuIcon className="h-6 w-6" />
              </div>
              <div>
                <CardTitle className="text-lg font-semibold text-white">Network Lab</CardTitle>
                <CardDescription className="text-slate-400">Live testing suite for request queues</CardDescription>
              </div>
            </CardHeader>
            <CardContent className="space-y-4 pt-2">
              <p className="text-sm text-slate-400 leading-relaxed">
                Click below to verify that your credentials pass through the Axios interceptor and talk directly to your Spring Boot controller layer.
              </p>
              
              <Button
                onClick={handleTestSecureEndpoint}
                disabled={isTestingApi}
                className="w-full bg-blue-600 hover:bg-blue-500 text-white gap-2 cursor-pointer"
              >
                {isTestingApi ? (
                  <>
                    <RefreshCwIcon className="h-4 w-4 animate-spin" />
                    Querying Secure Route...
                  </>
                ) : (
                  "Test Secure Endpoint"
                )}
              </Button>

              {apiResponse && (
                <div className="mt-3 rounded-md bg-slate-950 p-3 border border-slate-800 text-xs font-mono text-slate-300 overflow-x-auto break-all">
                  {apiResponse}
                </div>
              )}
            </CardContent>
          </Card>

        </div>
      </div>
    </div>
  );
}