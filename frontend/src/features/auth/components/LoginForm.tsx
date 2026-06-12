import React, { useState } from 'react';
import { Link } from 'react-router-dom';
import axios from 'axios';
import { login as loginService } from '../../../services/auth.service';
import { useAuth } from '../../../store/authContext';

export const LoginForm = () => {
    const [username, setUsername] = useState('');
    const [password, setPassword] = useState('');
    const [error, setError] = useState('');
    const { login } = useAuth();

    const handleSubmit = async (e: React.FormEvent) => {
        e.preventDefault();
        setError('');

        try {
            const response = await loginService({ username, password });
            login(
                { id_usuario: response.id_usuario, usuario: response.usuario, rol: response.rol },
                response.token,

            );
        } catch (err) {
            if (axios.isAxiosError(err) && err.response?.data) {
                // Capturamos la estructura estándar de tu backend (status, message, subErrors)
                const backendError = err.response.data;
                let errorMessage = backendError.message || 'Error en la validación de credenciales';

                // Si el backend envía el array subErrors (validación de campos), mostramos el primero
                if (backendError.subErrors && backendError.subErrors.length > 0) {
                    errorMessage = `${backendError.subErrors[0].field}: ${backendError.subErrors[0].message}`;
                }
                setError(errorMessage);
            } else {
                setError('Error de conexión con el servidor.');
            }
        }
    };

    return (
        <div className="flex justify-center items-center h-screen bg-gray-100">
            <form onSubmit={handleSubmit} className="flex flex-col gap-5 w-full max-w-md p-8 bg-white shadow-lg rounded-xl">
                <h2 className="text-3xl font-bold text-center text-gray-800">MoviGestión</h2>

                {error && <div className="p-3 text-sm text-red-700 bg-red-100 rounded-md break-words">{error}</div>}

                <div className="flex flex-col gap-1">
                    <label className="text-sm font-medium text-gray-700">Usuario</label>
                    <input
                        type="text"
                        value={username}
                        onChange={(e) => setUsername(e.target.value)}
                        className="border border-gray-300 p-2.5 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500"
                        required
                    />
                </div>

                <div className="flex flex-col gap-1">
                    <label className="text-sm font-medium text-gray-700">Contraseña</label>
                    <input
                        type="password"
                        value={password}
                        onChange={(e) => setPassword(e.target.value)}
                        className="border border-gray-300 p-2.5 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500"
                        required
                    />
                </div>

                <button
                    type="submit"
                    className="mt-2 bg-blue-600 text-white font-semibold p-3 rounded-lg hover:bg-blue-700 transition-colors"
                >
                    Iniciar Sesión
                </button>

                <div className="text-center mt-2">
                    <Link to="/register" className="text-sm text-blue-600 hover:underline">
                        Registrar cuenta de Administrador
                    </Link>
                </div>
            </form>
        </div>
    );
};