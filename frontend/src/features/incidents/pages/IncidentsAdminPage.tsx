import { useEffect, useState } from 'react';
import { getIncidents } from '../../../services/incident.service';
import type { Incident } from '../../../types';

export const IncidentsAdminPage = () => {
    const [incidents, setIncidents] = useState<Incident[]>([]);

    useEffect(() => {
        const loadData = async () => {
            try {
                const data = await getIncidents();
                setIncidents(data);
            } catch (error) {
                console.error(error);
            }
        };

        loadData();
    }, []);

    return (
        <div>
            <h2 className="text-2xl font-bold text-gray-800 mb-6">Consola de Incidencias en Ruta</h2>

            <div className="bg-white rounded-xl shadow-sm border border-gray-100 overflow-hidden">
                <table className="w-full text-left border-collapse">
                    <thead>
                    <tr className="bg-gray-50 border-b border-gray-200 text-gray-600">
                        <th className="p-4 font-semibold text-sm">ID</th>
                        <th className="p-4 font-semibold text-sm">Fecha</th>
                        <th className="p-4 font-semibold text-sm">Viaje / Unidad</th>
                        <th className="p-4 font-semibold text-sm">Clasificación</th>
                        <th className="p-4 font-semibold text-sm">Descripción</th>
                        <th className="p-4 font-semibold text-sm">Ubicación</th>
                    </tr>
                    </thead>
                    <tbody>
                    {incidents.map((inc) => (
                        <tr key={inc.id_incidencia} className="border-b border-gray-100 hover:bg-gray-50">
                            <td className="p-4 text-sm font-medium text-gray-800">#{inc.id_incidencia}</td>
                            <td className="p-4 text-sm text-gray-600">
                                {new Date(inc.fecha_reporte).toLocaleString()}
                            </td>
                            <td className="p-4 text-sm text-gray-600">
                                Envío #{inc.id_envio} <br/> Vehículo #{inc.id_vehiculo}
                            </td>
                            <td className="p-4">
                  <span className="px-3 py-1 rounded-full text-xs font-bold bg-red-100 text-red-700">
                    {inc.clasificacion}
                  </span>
                            </td>
                            <td className="p-4 text-sm text-gray-600 max-w-xs truncate" title={inc.descripcion}>
                                {inc.descripcion}
                            </td>
                            <td className="p-4 text-sm text-gray-600">
                                {inc.latitud_reporte}, {inc.longitud_reporte}
                            </td>
                        </tr>
                    ))}
                    {incidents.length === 0 && (
                        <tr>
                            <td colSpan={6} className="p-8 text-center text-gray-500">
                                No hay incidencias registradas
                            </td>
                        </tr>
                    )}
                    </tbody>
                </table>
            </div>
        </div>
    );
};