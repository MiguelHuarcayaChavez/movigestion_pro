import { useState } from 'react';
import type { CreateDriverRequest } from '../../../types';

interface Props {
    onSubmit: (data: CreateDriverRequest) => Promise<void>;
}

export const DriverForm = ({ onSubmit }: Props) => {
    const [dni, setDni] = useState('');
    const [usuario, setUsuario] = useState('');
    const [contrasena, setContrasena] = useState('');
    const [celular, setCelular] = useState('');

    const handleSubmit = async (e: React.FormEvent) => {
        e.preventDefault();
        await onSubmit({ dni, usuario, contrasena, celular });
        setDni('');
        setUsuario('');
        setContrasena('');
        setCelular('');
    };

    return (
        <form onSubmit={handleSubmit} className="bg-white p-5 rounded-xl shadow-sm border border-gray-100 mb-6 flex flex-wrap gap-4 items-end">
            <div className="flex flex-col flex-1 min-w-[150px] gap-1">
                <label className="text-sm font-semibold text-gray-700">DNI</label>
                <input
                    type="text"
                    maxLength={8}
                    value={dni}
                    onChange={(e) => setDni(e.target.value)}
                    className="border border-gray-300 p-2.5 rounded-lg focus:ring-2 focus:ring-blue-500 outline-none"
                    required
                />
            </div>
            <div className="flex flex-col flex-1 min-w-[150px] gap-1">
                <label className="text-sm font-semibold text-gray-700">Usuario</label>
                <input
                    type="text"
                    value={usuario}
                    onChange={(e) => setUsuario(e.target.value)}
                    className="border border-gray-300 p-2.5 rounded-lg focus:ring-2 focus:ring-blue-500 outline-none"
                    required
                />
            </div>
            <div className="flex flex-col flex-1 min-w-[150px] gap-1">
                <label className="text-sm font-semibold text-gray-700">Contraseña Temporal</label>
                <input
                    type="password"
                    value={contrasena}
                    onChange={(e) => setContrasena(e.target.value)}
                    className="border border-gray-300 p-2.5 rounded-lg focus:ring-2 focus:ring-blue-500 outline-none"
                    required
                />
            </div>
            <div className="flex flex-col flex-1 min-w-[150px] gap-1">
                <label className="text-sm font-semibold text-gray-700">Celular</label>
                <input
                    type="text"
                    value={celular}
                    onChange={(e) => setCelular(e.target.value)}
                    className="border border-gray-300 p-2.5 rounded-lg focus:ring-2 focus:ring-blue-500 outline-none"
                    required
                />
            </div>
            <button
                type="submit"
                className="bg-green-600 text-white font-semibold px-6 py-2.5 rounded-lg hover:bg-green-700 transition-colors h-[46px]"
            >
                Registrar Conductor
            </button>
        </form>
    );
};