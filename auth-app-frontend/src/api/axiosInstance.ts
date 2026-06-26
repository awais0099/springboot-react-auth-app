import axios from "axios";
import type { InternalAxiosRequestConfig, AxiosResponse, AxiosError } from "axios";
import { useAuthStore } from "@/auth/store";

// Explicit structure matching standard Spring Boot validation / security error responses
interface SpringErrorPayload {
  status?: number;
  error?: string;
  message?: string;
  path?: string;
  timestamp?: string;
}

// Blueprint for queuing paused requests during token rotation
interface FailedRequest {
  resolve: (token: string) => void;
  reject: (error: unknown) => void;
}

// Global synchronization flags for interceptor queue orchestration
let isRefreshing = false;
let failedQueue: FailedRequest[] = [];

/**
 * Iterates through all suspended requests in the queue matrix.
 * Dispatches the fresh token to resolve them, or rejects them if rotation failed.
 */
const processQueue = (error: unknown, token: string | null = null) => {
  failedQueue.forEach((request) => {
    if (error) {
      request.reject(error);
    } else if (token) {
      request.resolve(token);
    }
  });
  failedQueue = [];
};

const apiClient = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || "http://localhost:8080/api/v1",
  timeout: 10000, // Senior Practice: Always specify network timeouts to prevent dangling connection streams
  headers: {
    "Content-Type": "application/json",
  },
  withCredentials: true, // Crucial for cross-origin cookie sharing or backend state handshakes
});

/**
 * 1. REQUEST INTERCEPTOR
 * Hardened to capture tokens out of RAM context instead of blocking disk IO operations.
 */
apiClient.interceptors.request.use(
  (config: InternalAxiosRequestConfig) => {
    // Senior Pattern: Extract state atomically from the Zustand global scope without causing re-renders
    const token = useAuthStore.getState().accessToken;
    
    if (token && config.headers) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    
    return config;
  },
  (error: AxiosError) => {
    return Promise.reject(error);
  }
);

/**
 * 2. RESPONSE INTERCEPTOR
 * Standardized to handle token rotation seamlessly via request queue suspension arrays.
 */
apiClient.interceptors.response.use(
  (response: AxiosResponse) => response,
  async (error: AxiosError) => {
    const originalRequest = error.config as InternalAxiosRequestConfig & { _retry?: boolean };
    const status = error.response?.status;

    // Handle Unauthenticated State (JWT Expired or Invalid Signature)
    if (status === 401 && originalRequest && !originalRequest._retry) {

      // If a refresh transaction is already running elsewhere, place this request into the holding queue
      if (isRefreshing) {
        return new Promise((resolve, reject) => {
          failedQueue.push({
            resolve: (token: string) => {
              if (originalRequest.headers) {
                originalRequest.headers.Authorization = `Bearer ${token}`;
              }
              resolve(apiClient(originalRequest));
            },
            reject: (err: unknown) => reject(err),
          });
        });
      }

      // Mark request to prevent endless execution loops if the handshake crashes
      originalRequest._retry = true;
      isRefreshing = true;

      try {
        console.log("Access token expired. Executing silent token rotation handshake...");
        
        // Target your standalone Spring Boot refresh endpoint
        const refreshResponse = await axios.post(
          `${apiClient.defaults.baseURL}/auth/refresh-token`,
          {},
          { withCredentials: true } // Transmits the secure HttpOnly refresh cookie automatically
        );

        const newAccessToken = refreshResponse.data.accessToken;

        // Sync the brand new access token back into Zustand application state memory
        useAuthStore.getState().updateAccessToken(newAccessToken);

        // Release all secondary requests caught in the holding pattern array
        processQueue(null, newAccessToken);

        // Re-execute the original request with the freshly minted authentication claim
        if (originalRequest.headers) {
          originalRequest.headers.Authorization = `Bearer ${newAccessToken}`;
        }
        return apiClient(originalRequest);

      } catch (refreshError) {
        // Hard failure: The refresh token itself is expired, blacklisted, or manipulated
        processQueue(refreshError, null);
        console.error("Refresh token validation failed. Initiating global session teardown...");
        
        useAuthStore.getState().logout();
        window.location.href = "/login";
        
        return Promise.reject(error.response?.data || error);
      } finally {
        isRefreshing = false;
      }
    }

    // Senior Pattern: Guarantee a uniform, user-friendly payload contract for standard application catches
    let standardizedError: SpringErrorPayload = {
      status: status || 500,
      message: "Something went wrong on our end. Please refresh the page and try again.",
    };

    if (error.response && error.response.data) {
      const backendData = error.response.data as SpringErrorPayload;
      standardizedError = {
        ...standardizedError,
        ...backendData,
        message: backendData.message || error.message,
      };
    } else if (error.request) {
      // Server is completely offline or dropped connection entirely
      standardizedError.message = "We're having trouble connecting to our servers. Please check your internet connection and try again.";
    } else {
      standardizedError.message = error.message;
    }

    return Promise.reject(standardizedError);
  }
);

export default apiClient;