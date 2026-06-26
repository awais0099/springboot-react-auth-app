import React, { useState } from "react";
import { Link, useNavigate } from "react-router"; 
import { Button } from "@/components/ui/button";
import {
  Card,
  CardContent,
  CardDescription,
  CardFooter,
  CardHeader,
  CardTitle,
} from "@/components/ui/card";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { Spinner } from "@/components/ui/spinner";
import { authService } from "@/api/authService";
import { AlertCircleIcon } from "lucide-react";
import { Alert, AlertDescription, AlertTitle } from "@/components/ui/alert";
import { useAuthStore } from "@/auth/store";

interface SpringApiError {
  status?: number;
  error?: string;
  message?: string;
  path?: string;
  timestamp?: string;
}

export default function LoginPage() {
  const [isLoading, setIsLoading] = useState(false);
  const [loginRequestError, setLoginRequestError] = useState<string | null>(null);
  
  const navigate = useNavigate();
  const loginAction = useAuthStore((state) => state.login);

  // Senior Pattern: Core submission workflow driven by native FormData extraction
  const handleCredentialsLogin = async (e: React.FormEvent<HTMLFormElement>) => {
    e.preventDefault();
    setIsLoading(true);
    setLoginRequestError(null);

    const formElement = e.currentTarget;
    const dataMatrix = new FormData(formElement);
    
    const email = dataMatrix.get("email") as string;
    const password = dataMatrix.get("password") as string;

    try {
      const loginResponse = await authService.login({ email, password });

      // Core Handshake: Dispatch tokens cleanly into Zustand context memory
      loginAction(loginResponse.user, loginResponse.accessToken);
      
      formElement.reset();
      navigate('/dashboard');
    } catch (error: unknown) {
      const apiError = error as SpringApiError;
      
      if (apiError && typeof apiError.message === "string") {
        // Captches standard Spring Security validation responses or custom BadCredentialsExceptions
        setLoginRequestError(apiError.message);
      } else if (error instanceof Error) {
        // Catches local browser disruptions or transport/network connection losses
        setLoginRequestError(error.message);
      } else {
        setLoginRequestError("An unexpected credential authorization anomaly occurred.");
      }
    } finally {
      setIsLoading(false);
    }
  };

  const handleOAuth2Login = (provider: "google" | "github") => {
    const backendUrl = import.meta.env.VITE_API_BASE_URL || "http://localhost:8080";
    window.location.href = `${backendUrl}/oauth2/authorization/${provider}`;
  };

  return (
    <div className="container flex min-h-[calc(100vh-4rem)] w-screen flex-col items-center justify-center py-10">
      <Card className="w-full max-w-[400px] shadow-md">
        <CardHeader className="space-y-1 text-center">
          <CardTitle className="text-2xl font-bold tracking-tight">
            Welcome back
          </CardTitle>
          <CardDescription>
            Enter your credentials or choose a provider
          </CardDescription>
        </CardHeader>
        <CardContent className="grid gap-4">
          
          {/* OAuth2 Actions */}
          <div className="grid grid-cols-2 gap-3">
            <Button
              variant="outline"
              className="w-full gap-2 cursor-pointer"
              onClick={() => handleOAuth2Login("google")}
              disabled={isLoading}
            >
              <svg className="h-4 w-4" aria-hidden="true" viewBox="0 0 24 24">
                <path d="M22.56 12.25c0-.78-.07-1.53-.2-2.25H12v4.26h5.92c-.26 1.37-1.04 2.53-2.21 3.31v2.77h3.57c2.08-1.92 3.28-4.74 3.28-8.09z" fill="#4285F4"/>
                <path d="M12 23c2.97 0 5.46-.98 7.28-2.66l-3.57-2.77c-.98.66-2.23 1.06-3.71 1.06-2.86 0-5.29-1.93-6.16-4.53H2.18v2.84C3.99 20.53 7.7 23 12 23z" fill="#34A853"/>
                <path d="M5.84 14.09c-.22-.66-.35-1.36-.35-2.09s.13-1.43.35-2.09V7.06H2.18C1.43 8.55 1 10.22 1 12s.43 3.45 1.18 4.94l2.85-2.22.81-.63z" fill="#FBBC05"/>
                <path d="M12 5.38c1.62 0 3.06.56 4.21 1.64l3.15-3.15C17.45 2.09 14.97 1 12 1 7.7 1 3.99 3.47 2.18 7.06l3.66 2.84c.87-2.6 3.3-4.53 6.16-4.53z" fill="#EA4335"/>
              </svg>
              Google
            </Button>

            <Button
              variant="outline"
              className="w-full gap-2 cursor-pointer"
              onClick={() => handleOAuth2Login("github")}
              disabled={isLoading}
            >
              <svg className="h-4 w-4" fill="currentColor" viewBox="0 0 24 24" aria-hidden="true">
                <path fillRule="evenodd" d="M12 2C6.477 2 2 6.484 2 12.017c0 4.425 2.865 8.18 6.839 9.504.5.092.682-.217.682-.483 0-.237-.008-.868-.013-1.703-2.782.605-3.369-1.343-3.369-1.343-.454-1.158-1.11-1.466-1.11-1.466-.908-.62.069-.608.069-.608 1.003.07 1.531 1.032 1.531 1.032.892 1.53 2.341 1.088 2.91.832.092-.647.35-1.088.636-1.338-2.22-.253-4.555-1.113-4.555-4.951 0-1.093.39-1.988 1.029-2.688-.103-.253-.446-1.272.098-2.65 0 0 .84-.27 2.75 1.026A9.564 9.564 0 0112 6.844c.85.004 1.705.115 2.504.337 1.909-1.296 2.747-1.027 2.747-1.027.546 1.379.202 2.398.1 2.651.64.7 1.028 1.595 1.028 2.688 0 3.848-2.339 4.695-4.566 4.943.359.309.678.92.678 1.855 0 1.338-.012 2.419-.012 2.747 0 .268.18.58.688.482A10.019 10.019 0 0022 12.017C22 6.484 17.522 2 12 2z" clipRule="evenodd"/>
              </svg>
              GitHub
            </Button>
          </div>

          <div className="relative my-2">
            <div className="absolute inset-0 flex items-center">
              <span className="w-full border-t" />
            </div>
            <div className="relative flex justify-center text-xs uppercase">
              <span className="bg-background px-2 text-muted-foreground">
                Or continue with email
              </span>
            </div>
          </div>

          {loginRequestError && (
            <Alert variant="destructive" className="max-w-md">
              <AlertCircleIcon className="h-4 w-4" />
              <AlertTitle>Authentication Failed</AlertTitle>
              <AlertDescription>{loginRequestError}</AlertDescription>
            </Alert>
          )}

          {/* Form fields tracked purely by the DOM using matching name properties */}
          <form onSubmit={handleCredentialsLogin} className="grid gap-3">
            <div className="grid gap-1">
              <Label htmlFor="email">Email</Label>
              <Input
                id="email"
                name="email" // Required for entry point extraction
                type="email"
                placeholder="name@example.com"
                autoCapitalize="none"
                autoComplete="email"
                autoCorrect="off"
                disabled={isLoading}
                required
              />
            </div>
            <div className="grid gap-1">
              <div className="flex items-center justify-between">
                <Label htmlFor="password">Password</Label>
                <Link
                  to="/forgot-password"
                  className="text-xs text-muted-foreground hover:text-primary underline-offset-4 hover:underline"
                >
                  Forgot password?
                </Link>
              </div>
              <Input
                id="password"
                name="password" // Required for entry point extraction
                type="password"
                autoComplete="current-password"
                disabled={isLoading}
                required
              />
            </div>
            <Button
              type="submit"
              className="mt-2 w-full cursor-pointer"
              disabled={isLoading}
            >
              {isLoading ? (
                <span className="flex items-center gap-2">
                  <Spinner /> Authenticating session...
                </span>
              ) : (
                "Sign In with Email"
              )}
            </Button>
          </form>
        </CardContent>
        <CardFooter className="flex justify-center border-t py-4 text-sm text-muted-foreground">
          Don&apos;t have an account?{" "}
          <Link
            to="/signup"
            className="ml-1 text-primary underline-offset-4 hover:underline font-medium"
          >
            Sign up
          </Link>
        </CardFooter>
      </Card>
    </div>
  );
}