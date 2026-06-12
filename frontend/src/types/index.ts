export type Role = 'ADMIN' | 'DRIVER';

export type VehicleStatus = 'DISPONIBLE' | 'EN_RUTA' | 'EN_MANTENIMIENTO' | 'DE_BAJA';

export type UserStatus = 'ACTIVO' | 'DE_BAJA';

export type TripStatus = 'SIN_CONDUCTOR_ASIGNADO' | 'ASIGNADO' | 'EN_CAMINO' | 'COMPLETADO' | 'CANCELADO';

export type IncidentClassification = 'VEHICULO' | 'RUTA' | 'CARGA' | 'OTRO';

export interface User {
    id_usuario: number;
    usuario: string;
    rol: Role;
    dni?: string;
    celular?: string;
}

export interface AuthResponse {
    id_usuario: number;
    usuario: string;
    rol: Role;
    token: string;
}

export interface LoginRequest {
    username: string; // Antes: usuario
    password: string; // Antes: contrasena
}

export interface Vehicle {
    id_vehiculo: number;
    placa: string;
    estado: VehicleStatus;
    fechaVencimientoSoat: string;
    fechaVencimientoRevisionTecnica: string;
}

export interface CreateVehicleRequest {
    placa: string;
    fechaVencimientoSoat: string;
    fechaVencimientoRevisionTecnica: string;
}

export interface UpdateVehicleRequest {
    fechaVencimientoSoat?: string;
    fechaVencimientoRevisionTecnica?: string;
    estado?: VehicleStatus;
}

export interface Driver extends User {
    estado: UserStatus;
    fecha_creacion: string;
}

export interface CreateDriverRequest {
    dni: string;
    usuario: string;
    contrasena: string;
    celular: string;
}

export interface Trip {
    id_envio: number;
    id_administrador: number;
    id_transportista?: number;
    id_vehiculo?: number;
    destino_latitud: number;
    destino_longitud: number;
    estado: TripStatus;
    fecha_creacion: string;
    fecha_inicio?: string;
    fecha_entrega?: string;
}

export interface CreateTripRequest {
    idTransportista?: number;
    idVehiculo?: number;
    destinoLatitud: number;
    destinoLongitud: number;
}

export interface RegisterAdminRequest {
    dni: string;
    username: string; // Antes: usuario
    password: string; // Antes: contrasena
    celular: string;
}

export interface Incident {
    id_incidencia: number;
    id_envio: number;
    id_vehiculo: number;
    id_transportista: number;
    clasificacion: IncidentClassification;
    descripcion: string;
    latitud_reporte: number;
    longitud_reporte: number;
    fecha_reporte: string;
}

export interface CreateIncidentRequest {
    clasificacion: IncidentClassification;
    descripcion: string;
    latitudReporte: number;
    longitudReporte: number;
}

export interface DashboardMetrics {
    totalViajesMes: number;
    porcentajeEntregasATiempo: number;
    vehiculosEnMantenimiento: number;
    incidenciasCriticasActivas: number;
}