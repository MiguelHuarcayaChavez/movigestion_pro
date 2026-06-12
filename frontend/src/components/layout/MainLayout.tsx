import { Outlet, useNavigate, Link } from 'react-router-dom';
import { useAuth } from '../../store/authContext';

export const MainLayout = () => {
    const { user, logout } = useAuth();
    const navigate = useNavigate();

    const handleLogout = () => {
        logout();
        navigate('/login');
    };

    return (
        <div className="min-h-screen bg-gray-50 flex flex-col">
            <header className="bg-blue-600 text-white p-4 shadow-md flex justify-between items-center">
                <div className="flex items-center gap-8">
                    <h1 className="text-xl font-bold tracking-wide">MoviGestión</h1>
                    {user?.rol === 'ADMIN' && (
                        <nav className="flex gap-4 text-sm font-medium">
                            <Link to="/dashboard" className="hover:text-blue-200 transition-colors">Dashboard</Link>
                            <Link to="/vehicles" className="hover:text-blue-200 transition-colors">Flota</Link>
                            <Link to="/drivers" className="hover:text-blue-200 transition-colors">Conductores</Link>
                            <Link to="/trips" className="hover:text-blue-200 transition-colors">Viajes</Link>
                        </nav>
                    )}
                </div>
                <div className="flex items-center gap-4">
          <span className="text-sm font-medium">
            {user?.usuario} ({user?.rol})
          </span>
                    <button
                        onClick={handleLogout}
                        className="bg-blue-800 px-4 py-1.5 rounded-lg text-sm font-semibold hover:bg-blue-900 transition-colors"
                    >
                        Cerrar Sesión
                    </button>
                </div>
            </header>
            <main className="flex-1 p-6 max-w-7xl w-full mx-auto">
                <Outlet />
            </main>
        </div>
    );
};