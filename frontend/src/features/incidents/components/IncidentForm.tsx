import { useState } from 'react';
import type { CreateIncidentRequest, IncidentClassification } from '../../../types';

interface Props {
    onSubmit: (data: CreateIncidentRequest) => Promise<void>;
}

export const IncidentForm = ({ onSubmit }: Props) => {
    const [clasificacion, setClasificacion] = useState<IncidentClassification>('VEHICULO');
    const [descripcion, setDescripcion] = useState('');
    const [latitudReporte, setLatitudReporte] = useState('');
    const [longitudReporte, setLongitudReporte] = useState('');

    const handleSubmit = async (e: React.FormEvent) => {
        e.preventDefault();
        await onSubmit({
            clasificacion,
            descripcion,
            latitudReporte: Number(latitudReporte),
            longitudReporte: Number(longitudReporte),
        });
        setDescripcion('');
        setLatitudReporte('');
        setLongitudReporte('');
    };

    return (
        <form onSubmit={handleSubmit} className="bg-white p-5 rounded-xl shadow-sm border border-gray-200 mb-6 flex flex-col gap-4">
            <h3 className="text-lg font-bold text-gray-800">Reportar Nueva Incidencia</h3>
            <p className="text-xs text-gray-500 mb-2">Nota: Solo puedes reportar si tienes un viaje en curso (EN CAMINO).</p>

            <div className="flex flex-wrap gap-4">
                <div className="flex flex-col flex-1 min-w-[150px] gap-1">
                    <label className="text-sm font-semibold text-gray-700">Clasificación</label>
                    <select
                        value={clasificacion}
                        onChange={(e) => setClasificacion(e.target.value as IncidentClassification)}
                        className="border border-gray-300 p-2.5 rounded-lg focus:ring-2 focus:ring-red-500 outline-none bg-white"
                    >
                        <option value="VEHICULO">Vehículo</option>
                        <option value="RUTA">Ruta</option>
                        <option value="CARGA">Carga</option>
                        <option value="OTRO">Otro</option>
                    </select>
                </div>
                <div className="flex flex-col flex-1 min-w-[150px] gap-1">
                    <label className="text-sm font-semibold text-gray-700">Latitud Actual</label>
                    <input
                        type="number"
                        step="any"
                        value={latitudReporte}
                        onChange={(e) => setLatitudReporte(e.target.value)}
                        className="border border-gray-300 p-2.5 rounded-lg focus:ring-2 focus:ring-red-500 outline-none"
                        required
                    />
                </div>
                <div className="flex flex-col flex-1 min-w-[150px] gap-1">
                    <label className="text-sm font-semibold text-gray-700">Longitud Actual</label>
                    <input
                        type="number"
                        step="any"
                        value={longitudReporte}
                        onChange={(e) => setLongitudReporte(e.target.value)}
                        className="border border-gray-300 p-2.5 rounded-lg focus:ring-2 focus:ring-red-500 outline-none"
                        required
                    />
                </div>
            </div>

            <div className="flex flex-col gap-1">
                <label className="text-sm font-semibold text-gray-700">Descripción detallada</label>
                <textarea
                    value={descripcion}
                    onChange={(e) => setDescripcion(e.target.value)}
                    rows={3}
                    className="border border-gray-300 p-2.5 rounded-lg focus:ring-2 focus:ring-red-500 outline-none resize-none"
                    required
                />
            </div>

            <button
                type="submit"
                className="bg-red-600 text-white font-semibold px-6 py-2.5 rounded-lg hover:bg-red-700 transition-colors w-full sm:w-auto self-end mt-2"
            >
                Enviar Reporte
            </button>
        </form>
    );
};