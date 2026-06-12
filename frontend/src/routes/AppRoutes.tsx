import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';
import { useAuth } from '../store/authContext';
import { ProtectedRoute } from '../components/protected/ProtectedRoute';
import { MainLayout } from '../components/layout/MainLayout';
import { LoginForm } from '../features/auth/components/LoginForm';
import { RegisterAdminForm } from '../features/auth/components/RegisterAdminForm';
import { VehiclesListPage } from '../features/vehicles/pages/VehiclesListPage';
import { DriversListPage } from '../features/drivers/pages/DriversListPage';
import { TripsAdminPage } from '../features/trips/pages/TripsAdminPage';
import { MyTripsDriverPage } from '../features/trips/pages/MyTripsDriverPage';
import { IncidentsAdminPage } from '../features/incidents/pages/IncidentsAdminPage';
import { MyIncidentsPage } from '../features/incidents/pages/MyIncidentsPage';
import { DashboardPage } from '../features/dashboard/pages/DashboardPage';

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
                        <Route path="/incidents" element={<IncidentsAdminPage />} />
                    </Route>

                    <Route element={<ProtectedRoute allowedRoles={['DRIVER']} />}>
                        <Route path="/my-trips" element={<MyTripsDriverPage />} />
                        <Route path="/my-incidents" element={<MyIncidentsPage />} />
                    </Route>
                </Route>

                <Route path="*" element={<Navigate to="/login" replace />} />
            </Routes>
        </BrowserRouter>
    );
};