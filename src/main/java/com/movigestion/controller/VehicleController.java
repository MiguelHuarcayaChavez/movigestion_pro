package com.movigestion.controller;

import com.movigestion.dto.request.CreateVehicleRequestDTO;
import com.movigestion.dto.request.UpdateVehicleRequestDTO;
import com.movigestion.dto.response.VehicleResponseDTO;
import com.movigestion.entity.VehicleStatusEnum;
import com.movigestion.service.VehicleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/vehicles")
@RequiredArgsConstructor
@Tag(name = "Vehicles", description = "Gestión del catálogo de vehículos y su estado operativo")
public class VehicleController {

    private final VehicleService vehicleService;

    @Operation(summary = "Registrar Vehículo", description = "Crea una nueva unidad de transporte en estado DISPONIBLE por defecto.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Vehículo creado exitosamente"),
            @ApiResponse(responseCode = "400", description = "Error de validación en los datos de entrada"),
            @ApiResponse(responseCode = "409", description = "La placa ingresada ya se encuentra registrada")
    })
    @PostMapping
    public ResponseEntity<VehicleResponseDTO> create(@Valid @RequestBody CreateVehicleRequestDTO request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(vehicleService.create(request));
    }

    @Operation(summary = "Listar Vehículos", description = "Retorna el catálogo de vehículos permitiendo filtrar opcionalmente por estado.")
    @ApiResponse(responseCode = "200", description = "Lista de vehículos retornada con éxito")
    @GetMapping
    public ResponseEntity<List<VehicleResponseDTO>> findAll(@RequestParam(required = false) VehicleStatusEnum estado) {
        return ResponseEntity.ok(vehicleService.findAll(estado));
    }

    @Operation(summary = "Actualizar Vehículo", description = "Modifica los datos operativos de un vehículo. Bloqueado si está EN_RUTA.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Vehículo actualizado exitosamente"),
            @ApiResponse(responseCode = "400", description = "Error de validación en los datos de entrada"),
            @ApiResponse(responseCode = "404", description = "Vehículo no encontrado"),
            @ApiResponse(responseCode = "422", description = "Regla de negocio violada (ej. intentar modificar un vehículo EN_RUTA)")
    })
    @PutMapping("/{id}")
    public ResponseEntity<VehicleResponseDTO> update(
            @PathVariable Integer id,
            @Valid @RequestBody UpdateVehicleRequestDTO request) {
        return ResponseEntity.ok(vehicleService.update(id, request));
    }

    @Operation(summary = "Dar de Baja Vehículo", description = "Realiza un borrado lógico del vehículo (cambia estado a DE_BAJA). Bloqueado si está EN_RUTA.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Vehículo dado de baja exitosamente"),
            @ApiResponse(responseCode = "404", description = "Vehículo no encontrado"),
            @ApiResponse(responseCode = "422", description = "Regla de negocio violada (ej. intentar dar de baja un vehículo EN_RUTA)")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        vehicleService.delete(id);
        return ResponseEntity.ok().build();
    }
}
