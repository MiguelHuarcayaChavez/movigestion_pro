package com.movigestion.controller;

import com.movigestion.dto.request.CreateTripRequestDTO;
import com.movigestion.dto.request.UpdateTripStatusDTO;
import com.movigestion.dto.response.TripResponseDTO;
import com.movigestion.entity.Role;
import com.movigestion.entity.Trip;
import com.movigestion.entity.TripStatusEnum;
import com.movigestion.entity.User;
import com.movigestion.exception.ResourceNotFoundException;
import com.movigestion.repository.TripRepository;
import com.movigestion.repository.UserRepository;
import com.movigestion.service.TripService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/trips")
@RequiredArgsConstructor
@Tag(name = "Trips", description = "Gestión de Viajes y Envíos de la Flota")
public class TripController {

    private final TripService tripService;
    private final UserRepository userRepository;
    private final TripRepository tripRepository;

    @Operation(summary = "Crear Viaje", description = "Asigna un conductor y un vehículo a un nuevo viaje.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Viaje creado exitosamente"),
            @ApiResponse(responseCode = "400", description = "Error en las coordenadas o formato"),
            @ApiResponse(responseCode = "422", description = "Restricciones operativas (ej. SOAT vencido, unidad ocupada)")
    })
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<TripResponseDTO> create(@Valid @RequestBody CreateTripRequestDTO request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(tripService.createTrip(request));
    }

    @Operation(summary = "Listar Viajes", description = "Lista todos los viajes globales del sistema.")
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<TripResponseDTO>> findAll() {
        return ResponseEntity.ok(tripService.findAllTrips());
    }

    @Operation(summary = "Mis Viajes", description = "Lista los viajes del conductor activo según el scope (active/pending).")
    @GetMapping("/my-trips")
    @PreAuthorize("hasRole('DRIVER')")
    public ResponseEntity<List<TripResponseDTO>> getMyTrips(@RequestParam(defaultValue = "active") String scope) {
        User driver = getCurrentUser();
        return ResponseEntity.ok(tripService.getDriverTrips(driver.getId(), scope));
    }

    @Operation(summary = "Actualizar Estado del Viaje", description = "Avanza el viaje en su máquina de estados permitida.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Estado actualizado exitosamente"),
            @ApiResponse(responseCode = "403", description = "Acceso denegado (ej. conductor modifica viaje ajeno o cancela viaje)"),
            @ApiResponse(responseCode = "404", description = "Viaje no encontrado"),
            @ApiResponse(responseCode = "422", description = "Transición de estado inválida")
    })
    @PatchMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMIN') or hasRole('DRIVER')")
    public ResponseEntity<TripResponseDTO> updateStatus(@PathVariable Integer id, @Valid @RequestBody UpdateTripStatusDTO request) {
        User currentUser = getCurrentUser();

        if (currentUser.getRol() == Role.DRIVER) {
            if (request.getNuevoEstado() == TripStatusEnum.CANCELADO) {
                throw new AccessDeniedException("Solo los administradores pueden cancelar un viaje.");
            }
            Trip trip = tripRepository.findById(id)
                    .orElseThrow(() -> new ResourceNotFoundException("Viaje no encontrado"));
                    
            if (trip.getDriver() == null || !trip.getDriver().getId().equals(currentUser.getId())) {
                throw new AccessDeniedException("El viaje no le pertenece a este conductor.");
            }
        }

        return ResponseEntity.ok(tripService.updateTripStatus(id, request));
    }

    private User getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return userRepository.findByUsername(auth.getName())
                .orElseThrow(() -> new AccessDeniedException("Usuario no autenticado correctamente."));
    }
}
