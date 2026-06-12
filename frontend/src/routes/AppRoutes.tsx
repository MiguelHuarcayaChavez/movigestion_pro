import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';
import { useAuth } from '../store/authContext';
import { ProtectedRoute } from '../components/protected/ProtectedRoute';
import { MainLayout } from '../components/layout/MainLayout';
import { LoginForm } from '../features/auth/components/LoginForm';
import { RegisterAdminForm } from '../features/auth/components/RegisterAdminForm';
import { VehiclesListPage } from '../features/vehicles/pages/VehiclesListPage';
import { DriversListPage } from '../features/drivers/pages/DriversListPage';
import { TripsAdminPage } from '../features/trips/pages/TripsAdminPage';

const DashboardPage = () => <div className="text-2xl font-bold">Panel de Administrador</div>;
const MyTripsDriverPage = () => <div className="text-2xl font-bold">Mis Viajes Activos</div>;

export const AppRoutes = () => {
    const { user } = useAuth();

    return (
        <BrowserRouter>
            <Routes>
                <Route
                    path="/login"
                    element={user ? <Navigate to={user.rol === 'ADMIN' ? '/dashboard' : '/my-trips'} replace /> : <LoginForm />}
                />
                <Route
                    path="/register"
                    element={user ? <Navigate to={user.rol === 'ADMIN' ? '/dashboard' : '/my-trips'} replace /> : <RegisterAdminForm />}
                />

                <Route element={<MainLayout />}>
                    <Route element={<ProtectedRoute allowedRoles={['ADMIN']} />}>
                        <Route path="/dashboard" element={<DashboardPage />} />
                        <Route path="/vehicles" element={<VehiclesListPage />} />
                        <Route path="/drivers" element={<DriversListPage />} />
                        <Route path="/trips" element={<TripsAdminPage />} />
                    </Route>

                    <Route element={<ProtectedRoute allowedRoles={['DRIVER']} />}>
                        <Route path="/my-trips" element={<MyTripsDriverPage />} />
                    </Route>
                </Route>

                <Route path="*" element={<Navigate to="/login" replace />} />
            </Routes>
        </BrowserRouter>
    );
};