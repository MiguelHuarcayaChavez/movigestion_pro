import { api } from './api';
import type { Driver, CreateDriverRequest } from '../types';

export const getDrivers = async (): Promise<Driver[]> => {
    const response = await api.get<Driver[]>('/drivers');
    return response.data;
};

export const createDriver = async (data: CreateDriverRequest): Promise<Driver> => {
    const response = await api.post<Driver>('/drivers', data);
    return response.data;
};