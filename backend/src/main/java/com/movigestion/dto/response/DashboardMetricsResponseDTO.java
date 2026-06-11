package com.movigestion.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class DashboardMetricsResponseDTO {
    private Long totalViajesMes;
    private Double porcentajeEntregasATiempo;
    private Long vehiculosEnMantenimiento;
    private Long vehiculosDisponibles;
    private Long incidenciasCriticasActivas;
    private Long conductoresEnRuta;
}
