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

interface SpringApiError {
  status?: string;
  statusCode?: number;
  message?: string;
}

export default function Signup() {
  const [isLoading, setIsLoading] = useState(false);
  const [signupRequestError, setSignupRequestError] = useState<string | null>(null);

  const navigate = useNavigate();

  const handleSubmit = async (e: React.FormEvent<HTMLFormElement>) => {
    e.preventDefault();
    setIsLoading(true);
    setSignupRequestError(null);

    // Senior Pattern: Extract input values natively from the DOM. 
    // This avoids forcing React to track state changes on every single keystroke.
    const formElement = e.currentTarget;
    const dataMatrix = new FormData(formElement);
    
    const name = dataMatrix.get("name") as string;
    const email = dataMatrix.get("email") as string;
    const password = dataMatrix.get("password") as string;

    try {
      await authService.register({
        name,
        email,
        password,
        image: null,
        enable: true,
      });

      formElement.reset(); // Safely clears native inputs
      navigate("/login");
    } catch (error: unknown) {
      // Senior Practice: Strict compile-time type guard instead of using 'any'
      const apiError = error as SpringApiError;
      
      if (apiError && typeof apiError.message === "string") {
        // Catches your structured Spring Boot backend message: "User with given email already exists"
        setSignupRequestError(apiError.message);
      } else if (error instanceof Error) {
        // Catches a client-side JavaScript runtime or network failure
        setSignupRequestError(error.message);
      } else {
        setSignupRequestError("An unexpected network anomaly occurred.");
      }
    } finally {
      setIsLoading(false);
    }
  };

  const handleOAuth2Signup = (provider: "google" | "github") => {
    const backendUrl = import.meta.env.VITE_API_BASE_URL || "http://localhost:8080";
    window.location.href = `${backendUrl}/oauth2/authorization/${provider}`;
  };

  return (
    <div className="container flex min-h-[calc(100vh-4rem)] w-screen flex-col items-center justify-center py-10">
      <Card className="w-full max-w-[400px] shadow-md">
        <CardHeader className="space-y-1 text-center">
          <CardTitle className="text-2xl font-bold tracking-tight">
            Create an account
          </CardTitle>
          <CardDescription>
            Choose a provider or sign up with email
          </CardDescription>
        </CardHeader>
        <CardContent className="grid gap-4">
          
          {/* OAuth2 Providers */}
          <div className="grid grid-cols-2 gap-3">
            <Button
              variant="outline"
              onClick={() => handleOAuth2Signup("google")}
              className="cursor-pointer"
            >
              Google
            </Button>
            <Button
              variant="outline"
              onClick={() => handleOAuth2Signup("github")}
              className="cursor-pointer"
            >
              GitHub
            </Button>
          </div>

          <div className="relative">
            <div className="absolute inset-0 flex items-center">
              <span className="w-full border-t" />
            </div>
            <div className="relative flex justify-center text-xs uppercase">
              <span className="bg-background px-2 text-muted-foreground">
                Or
              </span>
            </div>
          </div>

          {signupRequestError && (
            <Alert variant="destructive" className="max-w-md">
              <AlertCircleIcon className="h-4 w-4" />
              <AlertTitle>Registration Blocked</AlertTitle>
              <AlertDescription>{signupRequestError}</AlertDescription>
            </Alert>
          )}

          {/* Form with clean native name bindings */}
          <form onSubmit={handleSubmit} className="grid gap-3">
            <div className="grid gap-1">
              <Label htmlFor="name">Full Name</Label>
              <Input
                id="name"
                name="name" // Crucial for FormData tracking
                placeholder="John Doe"
                required
                disabled={isLoading}
              />
            </div>
            <div className="grid gap-1">
              <Label htmlFor="email">Email</Label>
              <Input
                id="email"
                name="email" // Crucial for FormData tracking
                type="email"
                placeholder="name@example.com"
                required
                disabled={isLoading}
              />
            </div>
            <div className="grid gap-1">
              <Label htmlFor="password">Password</Label>
              <Input
                id="password"
                name="password" // Crucial for FormData tracking
                type="password"
                required
                disabled={isLoading}
              />
            </div>
            <Button type="submit" className="mt-2 w-full cursor-pointer" disabled={isLoading}>
              {isLoading ? (
                <span className="flex items-center gap-2">
                  <Spinner /> Processing account...
                </span>
              ) : (
                "Sign Up"
              )}
            </Button>
          </form>
        </CardContent>
        <CardFooter className="flex justify-center border-t py-4 text-sm text-muted-foreground">
          Already have an account?{" "}
          <Link
            to="/login"
            className="ml-1 text-primary underline-offset-4 hover:underline font-medium"
          >
            Sign in
          </Link>
        </CardFooter>
      </Card>
    </div>
  );
}