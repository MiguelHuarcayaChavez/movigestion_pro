import { useState } from 'react';
import type { CreateVehicleRequest } from '../../../types';

interface Props {
    onSubmit: (data: CreateVehicleRequest) => Promise<void>;
}

export const VehicleForm = ({ onSubmit }: Props) => {
    const [placa, setPlaca] = useState('');
    const [fechaVencimientoSoat, setFechaVencimientoSoat] = useState('');
    const [fechaVencimientoRevisionTecnica, setFechaVencimientoRevisionTecnica] = useState('');

    const handleSubmit = async (e: React.FormEvent) => {
        e.preventDefault();
        await onSubmit({ placa, fechaVencimientoSoat, fechaVencimientoRevisionTecnica });
        setPlaca('');
        setFechaVencimientoSoat('');
        setFechaVencimientoRevisionTecnica('');
    };

    return (
        <form onSubmit={handleSubmit} className="bg-white p-5 rounded-xl shadow-sm border border-gray-100 mb-6 flex flex-wrap gap-4 items-end">
            <div className="flex flex-col flex-1 min-w-[200px] gap-1">
                <label className="text-sm font-semibold text-gray-700">Placa</label>
                <input
                    type="text"
                    value={placa}
                    onChange={(e) => setPlaca(e.target.value.toUpperCase())}
                    placeholder="ABC-123"
                    className="border border-gray-300 p-2.5 rounded-lg focus:ring-2 focus:ring-blue-500 outline-none uppercase"
                    required
                />
            </div>
            <div className="flex flex-col flex-1 min-w-[200px] gap-1">
                <label className="text-sm font-semibold text-gray-700">Vencimiento SOAT</label>
                <input
                    type="date"
                    value={fechaVencimientoSoat}
                    onChange={(e) => setFechaVencimientoSoat(e.target.value)}
                    className="border border-gray-300 p-2.5 rounded-lg focus:ring-2 focus:ring-blue-500 outline-none"
                    required
                />
            </div>
            <div className="flex flex-col flex-1 min-w-[200px] gap-1">
                <label className="text-sm font-semibold text-gray-700">Revisión Técnica</label>
                <input
                    type="date"
                    value={fechaVencimientoRevisionTecnica}
                    onChange={(e) => setFechaVencimientoRevisionTecnica(e.target.value)}
                    className="border border-gray-300 p-2.5 rounded-lg focus:ring-2 focus:ring-blue-500 outline-none"
                    required
                />
            </div>
            <button
                type="submit"
                className="bg-green-600 text-white font-semibold px-6 py-2.5 rounded-lg hover:bg-green-700 transition-colors h-[46px]"
            >
                Registrar
            </button>
        </form>
    );
};