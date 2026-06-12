import { useEffect, useState } from 'react';
import { getDrivers, createDriver } from '../../../services/driver.service';
import { DriverForm } from '../components/DriverForm';
import type { Driver, CreateDriverRequest } from '../../../types';

export const DriversListPage = () => {
    const [drivers, setDrivers] = useState<Driver[]>([]);

    const loadDrivers = async () => {
        try {
            const data = await getDrivers();
            setDrivers(data);
        } catch (error) {
            console.error(error);
        }
    };

    useEffect(() => {
        loadDrivers();
    }, []);

    const handleCreate = async (data: CreateDriverRequest) => {
        try {
            await createDriver(data);
            loadDrivers();
        } catch (error) {
            console.error(error);
        }
    };

    return (
        <div>
            <h2 className="text-2xl font-bold text-gray-800 mb-6">Gestión de Transportistas</h2>

            <DriverForm onSubmit={handleCreate} />

            <div className="bg-white rounded-xl shadow-sm border border-gray-100 overflow-hidden">
                <table className="w-full text-left border-collapse">
                    <thead>
                    <tr className="bg-gray-50 border-b border-gray-200 text-gray-600">
                        <th className="p-4 font-semibold text-sm">Usuario</th>
                        <th className="p-4 font-semibold text-sm">DNI</th>
                        <th className="p-4 font-semibold text-sm">Celular</th>
                        <th className="p-4 font-semibold text-sm">Estado</th>
                    </tr>
                    </thead>
                    <tbody>
                    {drivers.map((d) => (
                        <tr key={d.id_usuario} className="border-b border-gray-100 hover:bg-gray-50">
                            <td className="p-4 font-medium text-gray-800">{d.usuario}</td>
                            <td className="p-4 text-sm text-gray-600">{d.dni}</td>
                            <td className="p-4 text-sm text-gray-600">{d.celular}</td>
                            <td className="p-4">
                  <span className={`px-3 py-1 rounded-full text-xs font-bold ${
                      d.estado === 'ACTIVO' ? 'bg-green-100 text-green-700' : 'bg-red-100 text-red-700'
                  }`}>
                    {d.estado}
                  </span>
                            </td>
                        </tr>
                    ))}
                    {drivers.length === 0 && (
                        <tr>
                            <td colSpan={4} className="p-8 text-center text-gray-500">
                                No hay transportistas registrados
                            </td>
                        </tr>
                    )}
                    </tbody>
                </table>
            </div>
        </div>
    );
};