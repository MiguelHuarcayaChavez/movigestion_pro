import { useEffect, useState, useCallback } from 'react';
import { getMyTrips, updateTripStatus } from '../../../services/trip.service';
import type { Trip, TripStatus } from '../../../types';

export const MyTripsDriverPage = () => {
    const [trips, setTrips] = useState<Trip[]>([]);
    const [error, setError] = useState('');

    const loadMyTrips = useCallback(async () => {
        try {
            const data = await getMyTrips();
            const activeTrips = data.filter(t => t.estado === 'ASIGNADO' || t.estado === 'EN_CAMINO');
            setTrips(activeTrips);
        } catch (err) {
            console.error(err);
            setError('No se pudieron cargar tus viajes.');
        }
    }, []);

    useEffect(() => {
        loadMyTrips();
    }, [loadMyTrips]);

    const handleStatusChange = async (id: number, newStatus: TripStatus) => {
        try {
            await updateTripStatus(id, newStatus);
            loadMyTrips();
        } catch (err) {
            console.error(err);
            setError('Error al actualizar el estado del viaje.');
        }
    };

    return (
        <div className="max-w-xl mx-auto">
            <h2 className="text-2xl font-bold text-gray-800 mb-6">Mis Viajes Activos</h2>

            {error && <div className="p-3 mb-4 text-sm text-red-700 bg-red-100 rounded-md">{error}</div>}

            <div className="flex flex-col gap-4">
                {trips.map((trip) => (
                    <div key={trip.id_envio} className="bg-white p-5 rounded-xl shadow-sm border border-gray-200">
                        <div className="flex justify-between items-start mb-4">
                            <div>
                                <span className="text-xs text-gray-500 font-semibold uppercase tracking-wider">Viaje #{trip.id_envio}</span>
                                <h3 className="text-lg font-bold text-gray-800 mt-1">Destino: {trip.destino_latitud}, {trip.destino_longitud}</h3>
                            </div>
                            <span className={`px-3 py-1 rounded-full text-xs font-bold ${
                                trip.estado === 'EN_CAMINO' ? 'bg-blue-100 text-blue-700' : 'bg-orange-100 text-orange-700'
                            }`}>
                {trip.estado.replace('_', ' ')}
              </span>
                        </div>

                        <div className="flex flex-col gap-3 mt-4 pt-4 border-t border-gray-100">
                            {trip.estado === 'ASIGNADO' && (
                                <button
                                    onClick={() => handleStatusChange(trip.id_envio, 'EN_CAMINO')}
                                    className="w-full bg-blue-600 text-white font-semibold py-3 rounded-lg hover:bg-blue-700 transition-colors"
                                >
                                    Iniciar Viaje
                                </button>
                            )}

                            {trip.estado === 'EN_CAMINO' && (
                                <button
                                    onClick={() => handleStatusChange(trip.id_envio, 'COMPLETADO')}
                                    className="w-full bg-green-600 text-white font-semibold py-3 rounded-lg hover:bg-green-700 transition-colors"
                                >
                                    Confirmar Entrega
                                </button>
                            )}
                        </div>
                    </div>
                ))}

                {trips.length === 0 && !error && (
                    <div className="bg-white p-8 rounded-xl shadow-sm border border-gray-100 text-center">
                        <p className="text-gray-500 font-medium">No tienes viajes pendientes ni en curso.</p>
                    </div>
                )}
            </div>
        </div>
    );
};