package com.movigestion.controller;

import com.movigestion.dto.request.CreateDriverRequestDTO;
import com.movigestion.dto.response.DriverResponseDTO;
import com.movigestion.entity.User;
import com.movigestion.repository.UserRepository;
import com.movigestion.service.DriverService;
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
@RequestMapping("/api/v1/drivers")
@RequiredArgsConstructor
@Tag(name = "Drivers", description = "Gestión del personal de conducción (Transportistas)")
public class DriverController {

    private final DriverService driverService;
    private final UserRepository userRepository;

    @Operation(summary = "Pre-registro de Transportista", description = "Crea un nuevo conductor. Restringido a Administradores.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Conductor creado exitosamente"),
            @ApiResponse(responseCode = "400", description = "Error de validación en el formato de los datos"),
            @ApiResponse(responseCode = "403", description = "No tiene permisos para realizar esta acción (solo ADMIN)"),
            @ApiResponse(responseCode = "409", description = "El DNI, usuario o celular ya se encuentran registrados")
    })
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<DriverResponseDTO> create(@Valid @RequestBody CreateDriverRequestDTO request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(driverService.createDriver(request));
    }

    @Operation(summary = "Listar Transportistas", description = "Retorna el catálogo completo de conductores activos. Restringido a Administradores.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lista de conductores retornada exitosamente"),
            @ApiResponse(responseCode = "403", description = "No tiene permisos para realizar esta acción (solo ADMIN)")
    })
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<DriverResponseDTO>> findAll() {
        return ResponseEntity.ok(driverService.findAllActiveDrivers());
    }

    @Operation(summary = "Ver Detalle de Transportista", description = "Retorna el perfil del conductor. ADMIN puede ver cualquiera. DRIVER solo puede ver su propio perfil.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Detalle del conductor retornado exitosamente"),
            @ApiResponse(responseCode = "403", description = "Acceso denegado. Intento de ataque IDOR bloqueado."),
            @ApiResponse(responseCode = "404", description = "Conductor no encontrado")
    })
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('DRIVER')")
    public ResponseEntity<DriverResponseDTO> findById(@PathVariable Integer id) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        // Extraer rol directamente para bloquear accesos IDOR
        boolean isDriver = auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_DRIVER"));

        if (isDriver) {
            // Se inyecta el UserRepository para obtener el ID real del JWT a partir del username
            User currentUser = userRepository.findByUsername(auth.getName())
                    .orElseThrow(() -> new AccessDeniedException("No se pudo verificar su identidad en el sistema."));

            if (!currentUser.getId().equals(id)) {
                throw new AccessDeniedException("No tiene los permisos necesarios para visualizar el perfil de otro usuario.");
            }
        }

        return ResponseEntity.ok(driverService.findDriverById(id));
    }
}
