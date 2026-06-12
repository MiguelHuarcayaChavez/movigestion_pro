/* eslint-disable react-refresh/only-export-components */
import { createContext, useState, useContext } from 'react';
import type { ReactNode } from 'react';
import type { User } from '../types';

interface AuthContextType {
    user: User | null;
    token: string | null;
    login: (user: User, token: string) => void;
    logout: () => void;
}

const AuthContext = createContext<AuthContextType | undefined>(undefined);

export const AuthProvider = ({ children }: { children: ReactNode }) => {
    const [user, setUser] = useState<User | null>(null);
    const [token, setToken] = useState<string | null>(localStorage.getItem('token'));

    const loginAction = (userData: User, authToken: string) => {
        setUser(userData);
        setToken(authToken);
        localStorage.setItem('token', authToken);
    };

    const logoutAction = () => {
        setUser(null);
        setToken(null);
        localStorage.removeItem('token');
    };

    return (
        <AuthContext.Provider value={{ user, token, login: loginAction, logout: logoutAction }}>
            {children}
        </AuthContext.Provider>
    );
};

export const useAuth = () => {
    const context = useContext(AuthContext);
    if (!context) {
        throw new Error('useAuth must be used within an AuthProvider');
    }
    return context;
};