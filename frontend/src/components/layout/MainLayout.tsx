import { Outlet, useNavigate, Link, useLocation } from 'react-router-dom';
import { useAuth } from '../../store/authContext';

export const MainLayout = () => {
    const { user, logout } = useAuth();
    const navigate = useNavigate();
    const location = useLocation();

    const handleLogout = () => {
        logout();
        navigate('/login');
    };

    // Función para resaltar la opción del menú actual
    const isActive = (path: string) => location.pathname === path;
    const linkClasses = (path: string) =>
        `block px-4 py-3 rounded-lg transition-colors font-medium text-sm ${
            isActive(path)
                ? 'bg-blue-700 text-white shadow-sm'
                : 'text-blue-100 hover:bg-blue-700 hover:text-white'
        }`;

    return (
        <div className="flex h-screen bg-gray-100 font-sans text-gray-900 overflow-hidden">

            {/* Barra Lateral (Aside) */}
            <aside className="w-64 bg-blue-900 text-white flex flex-col shadow-2xl z-10">
                <div className="p-6 border-b border-blue-800 text-center">
                    <h1 className="text-2xl font-bold tracking-wide">MoviGestión</h1>
                    <span className="text-xs text-blue-300 uppercase tracking-wider mt-1 block">
            Plataforma Logística
          </span>
                </div>

                <nav className="flex-1 p-4 space-y-2 overflow-y-auto">
                    {user?.rol === 'ADMIN' && (
                        <>
                            <Link to="/dashboard" className={linkClasses('/dashboard')}>📊 Dashboard</Link>
                            <Link to="/vehicles" className={linkClasses('/vehicles')}>🚚 Flota (Vehículos)</Link>
                            <Link to="/drivers" className={linkClasses('/drivers')}>👨‍✈️ Conductores</Link>
                            <Link to="/trips" className={linkClasses('/trips')}>🛣️ Viajes y Rutas</Link>
                            <Link to="/incidents" className={linkClasses('/incidents')}>⚠️ Incidencias</Link>
                        </>
                    )}

                    {user?.rol === 'DRIVER' && (
                        <>
                            <Link to="/my-trips" className={linkClasses('/my-trips')}>📍 Mis Viajes</Link>
                            <Link to="/my-incidents" className={linkClasses('/my-incidents')}>📝 Mis Reportes</Link>
                        </>
                    )}
                </nav>

                <div className="p-5 border-t border-blue-800 bg-blue-950">
                    <div className="mb-4 flex flex-col">
                        <span className="text-sm font-semibold truncate">👤 {user?.usuario}</span>
                        <span className="text-xs text-blue-400 mt-0.5">Rol: {user?.rol}</span>
                    </div>
                    <button
                        onClick={handleLogout}
                        className="w-full bg-red-500/10 text-red-400 border border-red-500/30 px-4 py-2 rounded-lg text-sm font-semibold hover:bg-red-500 hover:text-white transition-colors"
                    >
                        Cerrar Sesión
                    </button>
                </div>
            </aside>

            {/* Contenido Principal */}
            <main className="flex-1 overflow-y-auto bg-gray-50 p-8">
                <div className="max-w-7xl mx-auto">
                    <Outlet />
                </div>
            </main>

        </div>
    );
};