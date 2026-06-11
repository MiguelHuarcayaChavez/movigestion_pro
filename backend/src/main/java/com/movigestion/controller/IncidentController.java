package com.movigestion.controller;

import com.movigestion.dto.request.CreateIncidentRequestDTO;
import com.movigestion.dto.response.IncidentResponseDTO;
import com.movigestion.service.IncidentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/incidents")
@RequiredArgsConstructor
@Tag(name = "Incidents", description = "Emisión y consulta del histórico de incidencias logísticas")
public class IncidentController {

    private final IncidentService incidentService;

    @Operation(summary = "Registrar Incidencia", description = "Crea un nuevo reporte autoinyectando el viaje activo del conductor logueado.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Incidencia creada y registrada con éxito"),
            @ApiResponse(responseCode = "400", description = "Error de validación (ej. coordenadas inválidas o clasificación que no está en el ENUM)"),
            @ApiResponse(responseCode = "403", description = "No autorizado (exclusivo para ROLE_DRIVER)"),
            @ApiResponse(responseCode = "422", description = "El chofer no tiene un viaje activo en curso")
    })
    @PostMapping
    @PreAuthorize("hasRole('DRIVER')")
    public ResponseEntity<IncidentResponseDTO> create(@Valid @RequestBody CreateIncidentRequestDTO request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(incidentService.createIncident(request));
    }

    @Operation(summary = "Monitoreo Global de Incidencias", description = "Retorna el histórico completo de incidencias. Exclusivo para el dashboard de administración.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Listado de incidencias retornado exitosamente"),
            @ApiResponse(responseCode = "403", description = "No autorizado (exclusivo para ROLE_ADMIN)")
    })
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<IncidentResponseDTO>> findAll() {
        return ResponseEntity.ok(incidentService.findAllIncidents());
    }

    @Operation(summary = "Historial Personal de Incidencias", description = "Retorna exclusivamente los reportes emitidos por el conductor logueado (protección IDOR garantizada).")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Historial retornado exitosamente"),
            @ApiResponse(responseCode = "403", description = "No autorizado (exclusivo para ROLE_DRIVER)")
    })
    @GetMapping("/my-incidents")
    @PreAuthorize("hasRole('DRIVER')")
    public ResponseEntity<List<IncidentResponseDTO>> getMyIncidents() {
        return ResponseEntity.ok(incidentService.getDriverIncidents());
    }
}
