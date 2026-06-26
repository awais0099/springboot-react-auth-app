import apiClient from "./axiosInstance";

export interface RegisterRequest {
    name: string;
    email: string;
    password: string;
    image?: string | null;
    enable?: boolean;
}

export interface LoginRequest {
    email: string;
    password: string;
}


export const authService = {
    register: async (userData: RegisterRequest) => {
        const response = await apiClient.post("/auth/register", userData);
        return response.data;
    },

    login: async (loginData: LoginRequest) => {
        const response = await apiClient.post("/auth/login", loginData);
        return response.data;
    },

    testInterceptor: async (email: string) => {
        const response = await apiClient.get("/users/email/"+email);
        return response.data;
    }
}
