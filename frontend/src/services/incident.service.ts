import { api } from './api';
import type { Incident, CreateIncidentRequest } from '../types';

export const getIncidents = async (): Promise<Incident[]> => {
    const response = await api.get<Incident[]>('/incidents');
    return response.data;
};

export const getMyIncidents = async (): Promise<Incident[]> => {
    const response = await api.get<Incident[]>('/incidents/my-incidents');
    return response.data;
};

export const createIncident = async (data: CreateIncidentRequest): Promise<Incident> => {
    const response = await api.post<Incident>('/incidents', data);
    return response.data;
};