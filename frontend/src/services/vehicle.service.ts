import { api } from './api';
import type { Vehicle, CreateVehicleRequest, UpdateVehicleRequest } from '../types';

export const getVehicles = async (): Promise<Vehicle[]> => {
    const response = await api.get<Vehicle[]>('/vehicles');
    return response.data;
};

export const createVehicle = async (data: CreateVehicleRequest): Promise<Vehicle> => {
    const response = await api.post<Vehicle>('/vehicles', data);
    return response.data;
};

export const updateVehicle = async (id: number, data: UpdateVehicleRequest): Promise<Vehicle> => {
    const response = await api.put<Vehicle>(`/vehicles/${id}`, data);
    return response.data;
};

export const deleteVehicle = async (id: number): Promise<void> => {
    await api.delete(`/vehicles/${id}`);
};