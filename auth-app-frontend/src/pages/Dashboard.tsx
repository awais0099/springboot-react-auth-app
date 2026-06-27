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
import { ShieldCheckIcon, CpuIcon, RefreshCwIcon, LogOutIcon, KeyIcon, CopyIcon, CheckIcon, EyeIcon, EyeOffIcon } from "lucide-react";
import ApiDocsButton from "@/components/ui/ApiDocsButton";

export default function Dashboard() {
  const user = useAuthStore((state) => state.user);
  const accessToken = useAuthStore(state => state.accessToken);
  const logoutAction = useAuthStore((state) => state.logout);
  
  const [isTestingApi, setIsTestingApi] = useState(false);
  const [apiResponse, setApiResponse] = useState<string | null>(null);
  
  // UI States for Token Inspector
  const [showToken, setShowToken] = useState(false);
  const [copied, setCopied] = useState(false);

  // Safeguard: Ensure user exists before trying to destructure properties
  if (!user) return null;

  const handleCopyToken = async () => {
    if (!accessToken) return;
    try {
      await navigator.clipboard.writeText(accessToken);
      setCopied(true);
      setTimeout(() => setCopied(false), 2000);
    } catch (err) {
      console.error("Failed to copy token", err);
    }
  };

  const handleTestSecureEndpoint = async () => {
    setIsTestingApi(true);
    setApiResponse(null);

    try {
      const data = await authService.testInterceptor(user.email);
      setApiResponse(`Success! Server responded with: ${JSON.stringify(data)}`);
    } catch (error: any) {
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

        <ApiDocsButton />

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

        {/* New Full Width Section: Live Token Inspector */}
        <Card className="bg-slate-900 border-slate-800 text-slate-100">
          <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-4">
            <div className="flex items-center gap-4">
              <div className="rounded-lg bg-amber-500/10 p-2 text-amber-400">
                <KeyIcon className="h-6 w-6" />
              </div>
              <div>
                <CardTitle className="text-lg font-semibold text-white">Live Token Inspector</CardTitle>
                <CardDescription className="text-slate-400">Active memory state of your Access Token</CardDescription>
              </div>
            </div>
            
            <div className="flex gap-2">
              <Button
                variant="outline"
                size="sm"
                className="border-slate-700 hover:bg-slate-800 text-slate-300 gap-1.5 cursor-pointer"
                onClick={() => setShowToken(!showToken)}
              >
                {showToken ? <EyeOffIcon className="h-3.5 w-3.5" /> : <EyeIcon className="h-3.5 w-3.5" />}
                {showToken ? "Hide Token" : "Reveal Token"}
              </Button>
              
              <Button
                variant="outline"
                size="sm"
                disabled={!accessToken}
                className="border-slate-700 hover:bg-slate-800 text-slate-300 gap-1.5 cursor-pointer"
                onClick={handleCopyToken}
              >
                {copied ? <CheckIcon className="h-3.5 w-3.5 text-emerald-400" /> : <CopyIcon className="h-3.5 w-3.5" />}
                {copied ? "Copied!" : "Copy Token"}
              </Button>
            </div>
          </CardHeader>
          
          <CardContent>
            {accessToken ? (
              <div className="relative rounded-md bg-slate-950 border border-slate-800 p-4 font-mono text-xs tracking-tight select-all">
                {showToken ? (
                  <p className="text-amber-400/90 break-all leading-normal max-h-32 overflow-y-auto pr-2 custom-scrollbar">
                    {accessToken}
                  </p>
                ) : (
                  <p className="text-slate-600 tracking-widest break-all select-none">
                    {"•".repeat(Math.min(accessToken.length, 120))}
                  </p>
                )}
              </div>
            ) : (
              <div className="text-center py-4 text-sm text-slate-500 border border-dashed border-slate-800 rounded-md">
                No active Access Token found in application state.
              </div>
            )}
          </CardContent>
        </Card>

      </div>
    </div>
  );
}