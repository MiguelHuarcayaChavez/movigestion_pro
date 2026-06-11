package com.movigestion.service;

import com.movigestion.dto.response.DashboardMetricsResponseDTO;
import com.movigestion.entity.VehicleStatusEnum;
import com.movigestion.repository.IncidentRepository;
import com.movigestion.repository.TripRepository;
import com.movigestion.repository.VehicleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AnalyticsServiceImpl implements AnalyticsService {

    private final VehicleRepository vehicleRepository;
    private final TripRepository tripRepository;
    private final IncidentRepository incidentRepository;

    @Override
    @Transactional(readOnly = true)
    public DashboardMetricsResponseDTO getDashboardMetrics() {
        // Agregaciones Nativas
        long totalViajesMes = tripRepository.countTripsCurrentMonth();
        long entregasATiempo = tripRepository.countCompletedTripsCurrentMonth();
        long vehiculosEnMantenimiento = vehicleRepository.countByEstado(VehicleStatusEnum.EN_MANTENIMIENTO);
        long vehiculosDisponibles = vehicleRepository.countByEstado(VehicleStatusEnum.DISPONIBLE);
        long incidenciasCriticasActivas = incidentRepository.countIncidenciasCriticasActivas();
        long conductoresEnRuta = tripRepository.countDriversEnRuta();

        // BR-ANALYTICS: Protección Aritmética (Evitar División por Cero)
        double porcentajeEntregasATiempo = 0.0;
        if (totalViajesMes > 0) {
            porcentajeEntregasATiempo = ((double) entregasATiempo / totalViajesMes) * 100.0;
            // Redondear a 2 decimales
            porcentajeEntregasATiempo = Math.round(porcentajeEntregasATiempo * 100.0) / 100.0;
        }

        return DashboardMetricsResponseDTO.builder()
                .totalViajesMes(totalViajesMes)
                .porcentajeEntregasATiempo(porcentajeEntregasATiempo)
                .vehiculosEnMantenimiento(vehiculosEnMantenimiento)
                .vehiculosDisponibles(vehiculosDisponibles)
                .incidenciasCriticasActivas(incidenciasCriticasActivas)
                .conductoresEnRuta(conductoresEnRuta)
                .build();
    }
}
