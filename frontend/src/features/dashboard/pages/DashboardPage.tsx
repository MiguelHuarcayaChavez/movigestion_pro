import { useEffect, useState } from 'react';
import { getDashboardMetrics } from '../../../services/analytics.service';
import type { DashboardMetrics } from '../../../types';

export const DashboardPage = () => {
    const [metrics, setMetrics] = useState<DashboardMetrics | null>(null);
    const [error, setError] = useState('');

    useEffect(() => {
        const loadMetrics = async () => {
            try {
                const data = await getDashboardMetrics();
                setMetrics(data);
            } catch (err) {
                console.error(err);
                setError('No se pudieron cargar las métricas del dashboard.');
            }
        };

        loadMetrics();
    }, []);

    return (
        <div>
            <h2 className="text-2xl font-bold text-gray-800 mb-6">Panel de Control General</h2>

            {error && <div className="p-3 mb-6 text-sm text-red-700 bg-red-100 rounded-md">{error}</div>}

            {metrics ? (
                <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-6">
                    <div className="bg-white p-6 rounded-xl shadow-sm border border-gray-100 flex flex-col justify-center items-center text-center">
                        <span className="text-sm font-semibold text-gray-500 uppercase tracking-wider mb-2">Viajes del Mes</span>
                        <span className="text-4xl font-bold text-blue-600">{metrics.totalViajesMes}</span>
                    </div>

                    <div className="bg-white p-6 rounded-xl shadow-sm border border-gray-100 flex flex-col justify-center items-center text-center">
                        <span className="text-sm font-semibold text-gray-500 uppercase tracking-wider mb-2">Entregas a Tiempo</span>
                        <span className="text-4xl font-bold text-green-600">{metrics.porcentajeEntregasATiempo}%</span>
                    </div>

                    <div className="bg-white p-6 rounded-xl shadow-sm border border-gray-100 flex flex-col justify-center items-center text-center">
                        <span className="text-sm font-semibold text-gray-500 uppercase tracking-wider mb-2">Vehículos en Taller</span>
                        <span className="text-4xl font-bold text-orange-500">{metrics.vehiculosEnMantenimiento}</span>
                    </div>

                    <div className="bg-white p-6 rounded-xl shadow-sm border border-gray-100 flex flex-col justify-center items-center text-center">
                        <span className="text-sm font-semibold text-gray-500 uppercase tracking-wider mb-2">Alertas Críticas</span>
                        <span className="text-4xl font-bold text-red-600">{metrics.incidenciasCriticasActivas}</span>
                    </div>
                </div>
            ) : (
                !error && <div className="text-gray-500 p-8 text-center bg-white rounded-xl border border-gray-100">Cargando métricas...</div>
            )}
        </div>
    );
};