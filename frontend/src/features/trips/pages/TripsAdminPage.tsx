import { useEffect, useState } from 'react';
import { getTrips, createTrip, updateTripStatus } from '../../../services/trip.service';
import { getDrivers } from '../../../services/driver.service';
import { getVehicles } from '../../../services/vehicle.service';
import { TripForm } from '../components/TripForm';
import type { Trip, CreateTripRequest, Driver, Vehicle } from '../../../types';

export const TripsAdminPage = () => {
    const [trips, setTrips] = useState<Trip[]>([]);
    const [drivers, setDrivers] = useState<Driver[]>([]);
    const [vehicles, setVehicles] = useState<Vehicle[]>([]);

    const loadData = async () => {
        try {
            const [tripsData, driversData, vehiclesData] = await Promise.all([
                getTrips(),
                getDrivers(),
                getVehicles()
            ]);
            setTrips(tripsData);
            setDrivers(driversData);
            setVehicles(vehiclesData);
        } catch (error) {
            console.error(error);
        }
    };

    useEffect(() => {
        loadData();
    }, []);

    const handleCreate = async (data: CreateTripRequest) => {
        try {
            await createTrip(data);
            loadData();
        } catch (error) {
            console.error(error);
        }
    };

    const handleCancelTrip = async (id: number) => {
        if (!window.confirm('¿Está seguro de cancelar este viaje?')) return;
        try {
            await updateTripStatus(id, 'CANCELADO');
            loadData();
        } catch (error) {
            console.error(error);
        }
    };

    return (
        <div>
            <h2 className="text-2xl font-bold text-gray-800 mb-6">Programación de Viajes</h2>

            <TripForm drivers={drivers} vehicles={vehicles} onSubmit={handleCreate} />

            <div className="bg-white rounded-xl shadow-sm border border-gray-100 overflow-hidden">
                <table className="w-full text-left border-collapse">
                    <thead>
                    <tr className="bg-gray-50 border-b border-gray-200 text-gray-600">
                        <th className="p-4 font-semibold text-sm">ID</th>
                        <th className="p-4 font-semibold text-sm">Conductor</th>
                        <th className="p-4 font-semibold text-sm">Vehículo</th>
                        <th className="p-4 font-semibold text-sm">Destino (Lat, Lng)</th>
                        <th className="p-4 font-semibold text-sm">Estado</th>
                        <th className="p-4 font-semibold text-sm text-right">Acciones</th>
                    </tr>
                    </thead>
                    <tbody>
                    {trips.map((t) => (
                        <tr key={t.id_envio} className="border-b border-gray-100 hover:bg-gray-50">
                            <td className="p-4 text-sm font-medium text-gray-800">#{t.id_envio}</td>
                            <td className="p-4 text-sm text-gray-600">
                                {t.id_transportista ? drivers.find(d => d.id_usuario === t.id_transportista)?.usuario || t.id_transportista : '-'}
                            </td>
                            <td className="p-4 text-sm text-gray-600">
                                {t.id_vehiculo ? vehicles.find(v => v.id_vehiculo === t.id_vehiculo)?.placa || t.id_vehiculo : '-'}
                            </td>
                            <td className="p-4 text-sm text-gray-600">{t.destino_latitud}, {t.destino_longitud}</td>
                            <td className="p-4">
                  <span className={`px-3 py-1 rounded-full text-xs font-bold ${
                      t.estado === 'COMPLETADO' ? 'bg-green-100 text-green-700' :
                          t.estado === 'CANCELADO' ? 'bg-red-100 text-red-700' :
                              t.estado === 'EN_CAMINO' ? 'bg-blue-100 text-blue-700' :
                                  'bg-orange-100 text-orange-700'
                  }`}>
                    {t.estado.replace(/_/g, ' ')}
                  </span>
                            </td>
                            <td className="p-4 text-right">
                                <button
                                    onClick={() => handleCancelTrip(t.id_envio)}
                                    disabled={t.estado === 'COMPLETADO' || t.estado === 'CANCELADO'}
                                    className="text-xs px-3 py-1.5 bg-red-50 hover:bg-red-100 text-red-600 font-semibold rounded disabled:opacity-50"
                                >
                                    Cancelar
                                </button>
                            </td>
                        </tr>
                    ))}
                    {trips.length === 0 && (
                        <tr>
                            <td colSpan={6} className="p-8 text-center text-gray-500">
                                No hay viajes programados
                            </td>
                        </tr>
                    )}
                    </tbody>
                </table>
            </div>
        </div>
    );
};