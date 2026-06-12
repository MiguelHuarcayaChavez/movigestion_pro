import { api } from './api';
import type { AuthResponse, LoginRequest, RegisterAdminRequest } from '../types';

export const login = async (data: LoginRequest): Promise<AuthResponse> => {
    const response = await api.post<AuthResponse>('/auth/login', data);
    return response.data;
};

export const registerAdmin = async (data: RegisterAdminRequest): Promise<void> => {
    await api.post('/auth/register-admin', data);
};