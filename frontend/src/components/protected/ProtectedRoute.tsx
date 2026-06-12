import { Navigate, Outlet } from 'react-router-dom';
import { useAuth } from '../../store/authContext';
import type { Role } from '../../types';

interface Props {
    allowedRoles?: Role[];
}

export const ProtectedRoute = ({ allowedRoles }: Props) => {
    const { user, token } = useAuth();

    if (!token || !user) {
        return <Navigate to="/login" replace />;
    }

    if (allowedRoles && !allowedRoles.includes(user.rol)) {
        return <Navigate to={user.rol === 'ADMIN' ? '/dashboard' : '/my-trips'} replace />;
    }

    return <Outlet />;
};