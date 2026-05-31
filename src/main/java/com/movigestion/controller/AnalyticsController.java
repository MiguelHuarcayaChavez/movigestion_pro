package com.movigestion.controller;

import com.movigestion.dto.response.DashboardMetricsResponseDTO;
import com.movigestion.service.AnalyticsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/analytics")
@RequiredArgsConstructor
@Tag(name = "Analytics", description = "Métricas y KPIs del Dashboard central")
public class AnalyticsController {

    private final AnalyticsService analyticsService;

    @Operation(summary = "Obtener métricas del dashboard", description = "Retorna de forma unificada todos los contadores logísticos de la operación. Exclusivo para administradores.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Métricas calculadas y retornadas exitosamente"),
            @ApiResponse(responseCode = "403", description = "No autorizado. Restringido para el rol DRIVER.")
    })
    @GetMapping("/dashboard")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<DashboardMetricsResponseDTO> getDashboardMetrics() {
        return ResponseEntity.ok(analyticsService.getDashboardMetrics());
    }
}
