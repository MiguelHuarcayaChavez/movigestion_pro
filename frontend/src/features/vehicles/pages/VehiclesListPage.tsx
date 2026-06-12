import { useEffect, useState } from 'react';
import { getVehicles, createVehicle, updateVehicle, deleteVehicle } from '../../../services/vehicle.service';
import { VehicleForm } from '../components/VehicleForm';
import type { Vehicle, CreateVehicleRequest } from '../../../types';

export const VehiclesListPage = () => {
    const [vehicles, setVehicles] = useState<Vehicle[]>([]);

    const loadVehicles = async () => {
        try {
            const data = await getVehicles();
            setVehicles(data);
        } catch (error) {
            console.error(error);
        }
    };

    useEffect(() => {
        loadVehicles();
    }, []);

    const handleCreate = async (data: CreateVehicleRequest) => {
        try {
            await createVehicle(data);
            loadVehicles();
        } catch (error) {
            console.error(error);
        }
    };

    const handleToggleMaintenance = async (vehicle: Vehicle) => {
        try {
            const newStatus = vehicle.estado === 'EN_MANTENIMIENTO' ? 'DISPONIBLE' : 'EN_MANTENIMIENTO';
            await updateVehicle(vehicle.id_vehiculo, { estado: newStatus });
            loadVehicles();
        } catch (error) {
            console.error(error);
        }
    };

    const handleDelete = async (id: number) => {
        if (!window.confirm('¿Dar de baja este vehículo?')) return;
        try {
            await deleteVehicle(id);
            loadVehicles();
        } catch (error) {
            console.error(error);
        }
    };

    return (
        <div>
            <h2 className="text-2xl font-bold text-gray-800 mb-6">Gestión de Flota</h2>

            <VehicleForm onSubmit={handleCreate} />

            <div className="bg-white rounded-xl shadow-sm border border-gray-100 overflow-hidden">
                <table className="w-full text-left border-collapse">
                    <thead>
                    <tr className="bg-gray-50 border-b border-gray-200 text-gray-600">
                        <th className="p-4 font-semibold text-sm">Placa</th>
                        <th className="p-4 font-semibold text-sm">Estado</th>
                        <th className="p-4 font-semibold text-sm">Venc. SOAT</th>
                        <th className="p-4 font-semibold text-sm">Rev. Técnica</th>
                        <th className="p-4 font-semibold text-sm text-right">Acciones</th>
                    </tr>
                    </thead>
                    <tbody>
                    {vehicles.map((v) => (
                        <tr key={v.id_vehiculo} className="border-b border-gray-100 hover:bg-gray-50">
                            <td className="p-4 font-medium text-gray-800">{v.placa}</td>
                            <td className="p-4">
                  <span className={`px-3 py-1 rounded-full text-xs font-bold ${
                      v.estado === 'DISPONIBLE' ? 'bg-green-100 text-green-700' :
                          v.estado === 'EN_RUTA' ? 'bg-blue-100 text-blue-700' :
                              v.estado === 'EN_MANTENIMIENTO' ? 'bg-orange-100 text-orange-700' :
                                  'bg-red-100 text-red-700'
                  }`}>
                    {v.estado}
                  </span>
                            </td>
                            <td className="p-4 text-sm text-gray-600">{v.fechaVencimientoSoat}</td>
                            <td className="p-4 text-sm text-gray-600">{v.fechaVencimientoRevisionTecnica}</td>
                            <td className="p-4 text-right flex justify-end gap-2">
                                <button
                                    onClick={() => handleToggleMaintenance(v)}
                                    disabled={v.estado === 'EN_RUTA' || v.estado === 'DE_BAJA'}
                                    className="text-xs px-3 py-1.5 bg-gray-100 hover:bg-gray-200 text-gray-700 font-semibold rounded disabled:opacity-50"
                                >
                                    Mantenimiento
                                </button>
                                <button
                                    onClick={() => handleDelete(v.id_vehiculo)}
                                    disabled={v.estado === 'EN_RUTA' || v.estado === 'DE_BAJA'}
                                    className="text-xs px-3 py-1.5 bg-red-50 hover:bg-red-100 text-red-600 font-semibold rounded disabled:opacity-50"
                                >
                                    Dar Baja
                                </button>
                            </td>
                        </tr>
                    ))}
                    {vehicles.length === 0 && (
                        <tr>
                            <td colSpan={5} className="p-8 text-center text-gray-500">
                                No hay vehículos registrados
                            </td>
                        </tr>
                    )}
                    </tbody>
                </table>
            </div>
        </div>
    );
};