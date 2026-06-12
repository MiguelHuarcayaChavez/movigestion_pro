import { useEffect, useState } from 'react';
import axios from 'axios';
import { getMyIncidents, createIncident } from '../../../services/incident.service';
import { IncidentForm } from '../components/IncidentForm';
import type { Incident, CreateIncidentRequest } from '../../../types';

export const MyIncidentsPage = () => {
    const [incidents, setIncidents] = useState<Incident[]>([]);
    const [error, setError] = useState('');

    useEffect(() => {
        const loadIncidents = async () => {
            try {
                const data = await getMyIncidents();
                setIncidents(data);
            } catch (err) {
                console.error(err);
                setError('No se pudo cargar el historial de incidencias.');
            }
        };

        loadIncidents();
    }, []);

    const handleCreate = async (data: CreateIncidentRequest) => {
        setError('');
        try {
            // 1. Creamos la incidencia
            await createIncident(data);

            // 2. Refrescamos la tabla llamando al servicio de nuevo directamente
            const updatedData = await getMyIncidents();
            setIncidents(updatedData);
        } catch (err) {
            console.error(err);
            if (axios.isAxiosError(err) && err.response?.data) {
                setError(err.response.data.message || 'Error al reportar la incidencia.');
            } else {
                setError('Error al reportar la incidencia.');
            }
        }
    };

    return (
        <div className="max-w-3xl mx-auto">
            <h2 className="text-2xl font-bold text-gray-800 mb-6">Mis Reportes</h2>

            {error && <div className="p-3 mb-4 text-sm text-red-700 bg-red-100 rounded-md break-words">{error}</div>}

            <IncidentForm onSubmit={handleCreate} />

            <h3 className="text-xl font-bold text-gray-800 mb-4 mt-8">Historial</h3>
            <div className="flex flex-col gap-4">
                {incidents.map((inc) => (
                    <div key={inc.id_incidencia} className="bg-white p-4 rounded-xl shadow-sm border border-gray-200">
                        <div className="flex justify-between items-start mb-2">
              <span className="text-xs text-gray-500 font-semibold uppercase">
                {new Date(inc.fecha_reporte).toLocaleString()}
              </span>
                            <span className="px-3 py-1 rounded-full text-xs font-bold bg-red-100 text-red-700">
                {inc.clasificacion}
              </span>
                        </div>
                        <p className="text-gray-800 text-sm mt-2">{inc.descripcion}</p>
                        <div className="mt-3 text-xs text-gray-500">
                            📍 {inc.latitud_reporte}, {inc.longitud_reporte}
                        </div>
                    </div>
                ))}
                {incidents.length === 0 && (
                    <div className="text-center p-8 text-gray-500 bg-white rounded-xl border border-gray-100">
                        No tienes incidencias reportadas.
                    </div>
                )}
            </div>
        </div>
    );
};