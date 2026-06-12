import { api } from './api';
import type { Trip, CreateTripRequest, TripStatus } from '../types';

export const getTrips = async (): Promise<Trip[]> => {
    const response = await api.get<Trip[]>('/trips');
    return response.data;
};

export const getMyTrips = async (): Promise<Trip[]> => {
    const response = await api.get<Trip[]>('/trips/my-trips');
    return response.data;
};

export const createTrip = async (data: CreateTripRequest): Promise<Trip> => {
    const response = await api.post<Trip>('/trips', data);
    return response.data;
};

export const updateTripStatus = async (id: number, nuevoEstado: TripStatus): Promise<Trip> => {
    const response = await api.patch<Trip>(`/trips/${id}/status`, { nuevoEstado });
    return response.data;
};