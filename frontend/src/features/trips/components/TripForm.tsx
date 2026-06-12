import { useState } from 'react';
import type { CreateTripRequest, Driver, Vehicle } from '../../../types';

interface Props {
    drivers: Driver[];
    vehicles: Vehicle[];
    onSubmit: (data: CreateTripRequest) => Promise<void>;
}

export const TripForm = ({ drivers, vehicles, onSubmit }: Props) => {
    const [idTransportista, setIdTransportista] = useState('');
    const [idVehiculo, setIdVehiculo] = useState('');
    const [destinoLatitud, setDestinoLatitud] = useState('');
    const [destinoLongitud, setDestinoLongitud] = useState('');

    const handleSubmit = async (e: React.FormEvent) => {
        e.preventDefault();
        await onSubmit({
            idTransportista: idTransportista ? Number(idTransportista) : undefined,
            idVehiculo: idVehiculo ? Number(idVehiculo) : undefined,
            destinoLatitud: Number(destinoLatitud),
            destinoLongitud: Number(destinoLongitud),
        });
        setIdTransportista('');
        setIdVehiculo('');
        setDestinoLatitud('');
        setDestinoLongitud('');
    };

    return (
        <form onSubmit={handleSubmit} className="bg-white p-5 rounded-xl shadow-sm border border-gray-100 mb-6 flex flex-wrap gap-4 items-end">
            <div className="flex flex-col flex-1 min-w-[200px] gap-1">
                <label className="text-sm font-semibold text-gray-700">Conductor</label>
                <select
                    value={idTransportista}
                    onChange={(e) => setIdTransportista(e.target.value)}
                    className="border border-gray-300 p-2.5 rounded-lg focus:ring-2 focus:ring-blue-500 outline-none bg-white"
                >
                    <option value="">-- Sin Asignar --</option>
                    {drivers.map(d => (
                        <option key={d.id_usuario} value={d.id_usuario}>{d.usuario} ({d.dni})</option>
                    ))}
                </select>
            </div>
            <div className="flex flex-col flex-1 min-w-[200px] gap-1">
                <label className="text-sm font-semibold text-gray-700">Vehículo</label>
                <select
                    value={idVehiculo}
                    onChange={(e) => setIdVehiculo(e.target.value)}
                    className="border border-gray-300 p-2.5 rounded-lg focus:ring-2 focus:ring-blue-500 outline-none bg-white"
                >
                    <option value="">-- Sin Asignar --</option>
                    {vehicles.map(v => (
                        <option key={v.id_vehiculo} value={v.id_vehiculo}>{v.placa}</option>
                    ))}
                </select>
            </div>
            <div className="flex flex-col flex-1 min-w-[150px] gap-1">
                <label className="text-sm font-semibold text-gray-700">Latitud Destino</label>
                <input
                    type="number"
                    step="any"
                    value={destinoLatitud}
                    onChange={(e) => setDestinoLatitud(e.target.value)}
                    placeholder="-12.046374"
                    className="border border-gray-300 p-2.5 rounded-lg focus:ring-2 focus:ring-blue-500 outline-none"
                    required
                />
            </div>
            <div className="flex flex-col flex-1 min-w-[150px] gap-1">
                <label className="text-sm font-semibold text-gray-700">Longitud Destino</label>
                <input
                    type="number"
                    step="any"
                    value={destinoLongitud}
                    onChange={(e) => setDestinoLongitud(e.target.value)}
                    placeholder="-77.042754"
                    className="border border-gray-300 p-2.5 rounded-lg focus:ring-2 focus:ring-blue-500 outline-none"
                    required
                />
            </div>
            <button
                type="submit"
                className="bg-green-600 text-white font-semibold px-6 py-2.5 rounded-lg hover:bg-green-700 transition-colors h-[46px]"
            >
                Programar Viaje
            </button>
        </form>
    );
};