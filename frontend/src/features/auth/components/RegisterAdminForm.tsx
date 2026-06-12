import React, { useState } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import { registerAdmin } from '../../../services/auth.service';

export const RegisterAdminForm = () => {
    const [dni, setDni] = useState('');
    const [username, setUsername] = useState('');
    const [password, setPassword] = useState('');
    const [celular, setCelular] = useState('');
    const [error, setError] = useState('');
    const navigate = useNavigate();

    const handleSubmit = async (e: React.FormEvent) => {
        e.preventDefault();
        setError('');

        try {
            await registerAdmin({ dni, username, password, celular });
            navigate('/login');
        } catch (err) {
            console.error(err);
            setError('Error al registrar administrador. Verifique sus datos o si el usuario/DNI ya existe.');
        }
    };

    return (
        <div className="flex justify-center items-center h-screen bg-gray-100">
            <form onSubmit={handleSubmit} className="flex flex-col gap-5 w-full max-w-md p-8 bg-white shadow-lg rounded-xl">
                <h2 className="text-3xl font-bold text-center text-gray-800">Nuevo Administrador</h2>

                {error && <div className="p-3 text-sm text-red-700 bg-red-100 rounded-md break-words">{error}</div>}

                <div className="flex flex-col gap-1">
                    <label className="text-sm font-medium text-gray-700">DNI</label>
                    <input
                        type="text"
                        maxLength={8}
                        value={dni}
                        onChange={(e) => setDni(e.target.value)}
                        className="border border-gray-300 p-2.5 rounded-lg focus:ring-2 focus:ring-blue-500 outline-none"
                        required
                    />
                </div>

                <div className="flex flex-col gap-1">
                    <label className="text-sm font-medium text-gray-700">Usuario</label>
                    <input
                        type="text"
                        value={username}
                        onChange={(e) => setUsername(e.target.value)}
                        className="border border-gray-300 p-2.5 rounded-lg focus:ring-2 focus:ring-blue-500 outline-none"
                        required
                    />
                </div>

                <div className="flex flex-col gap-1">
                    <label className="text-sm font-medium text-gray-700">Contraseña</label>
                    <input
                        type="password"
                        value={password}
                        onChange={(e) => setPassword(e.target.value)}
                        className="border border-gray-300 p-2.5 rounded-lg focus:ring-2 focus:ring-blue-500 outline-none"
                        required
                    />
                </div>

                <div className="flex flex-col gap-1">
                    <label className="text-sm font-medium text-gray-700">Celular</label>
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
                    className="mt-2 bg-blue-600 text-white font-semibold p-3 rounded-lg hover:bg-blue-700 transition-colors"
                >
                    Registrarse
                </button>

                <div className="text-center mt-2">
                    <Link to="/login" className="text-sm text-blue-600 hover:underline">
                        ¿Ya tienes cuenta? Inicia sesión
                    </Link>
                </div>
            </form>
        </div>
    );
};