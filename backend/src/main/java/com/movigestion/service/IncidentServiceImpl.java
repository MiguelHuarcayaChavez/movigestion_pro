package com.movigestion.service;

import com.movigestion.dto.request.CreateIncidentRequestDTO;
import com.movigestion.dto.response.IncidentResponseDTO;
import com.movigestion.entity.Incident;
import com.movigestion.entity.Trip;
import com.movigestion.entity.TripStatusEnum;
import com.movigestion.entity.User;
import com.movigestion.exception.BusinessRuleViolationException;
import com.movigestion.exception.ResourceNotFoundException;
import com.movigestion.repository.IncidentRepository;
import com.movigestion.repository.TripRepository;
import com.movigestion.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class IncidentServiceImpl implements IncidentService {

    private final IncidentRepository incidentRepository;
    private final TripRepository tripRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public IncidentResponseDTO createIncident(CreateIncidentRequestDTO request) {
        String currentUsername = SecurityContextHolder.getContext().getAuthentication().getName();
        User driver = userRepository.findByUsername(currentUsername)
                .orElseThrow(() -> new ResourceNotFoundException("Conductor no autenticado o no encontrado."));

        // BR-INC-01: Inyección Automática de Contexto
        List<Trip> activeTrips = tripRepository.findByIdTransportistaAndEstado(driver.getId(), TripStatusEnum.EN_CAMINO);
        if (activeTrips.isEmpty()) {
            throw new BusinessRuleViolationException("No se puede emitir la incidencia: No cuenta con un viaje activo en ruta en este momento.");
        }

        Trip activeTrip = activeTrips.get(0);

        Incident newIncident = Incident.builder()
                .trip(activeTrip)
                .vehicle(activeTrip.getVehicle())
                .driver(driver)
                .clasificacion(request.getClasificacion())
                .descripcion(request.getDescripcion())
                .latitudReporte(request.getLatitudReporte())
                .longitudReporte(request.getLongitudReporte())
                .fechaReporte(LocalDateTime.now())
                .build();

        Incident savedIncident = incidentRepository.save(newIncident);
        return mapToDTO(savedIncident);
    }

    @Override
    @Transactional(readOnly = true)
    public List<IncidentResponseDTO> findAllIncidents() {
        return incidentRepository.findAll().stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<IncidentResponseDTO> getDriverIncidents() {
        String currentUsername = SecurityContextHolder.getContext().getAuthentication().getName();
        User driver = userRepository.findByUsername(currentUsername)
                .orElseThrow(() -> new ResourceNotFoundException("Conductor no autenticado o no encontrado."));

        return incidentRepository.findByIdTransportistaOrderByFechaReporteDesc(driver.getId())
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    private IncidentResponseDTO mapToDTO(Incident incident) {
        return IncidentResponseDTO.builder()
                .idIncidencia(incident.getIdIncidencia())
                .idEnvio(incident.getTrip() != null ? incident.getTrip().getIdEnvio() : null)
                .idVehiculo(incident.getVehicle() != null ? incident.getVehicle().getIdVehiculo() : null)
                .idTransportista(incident.getDriver() != null ? incident.getDriver().getId() : null)
                .clasificacion(incident.getClasificacion())
                .descripcion(incident.getDescripcion())
                .latitudReporte(incident.getLatitudReporte())
                .longitudReporte(incident.getLongitudReporte())
                .fechaReporte(incident.getFechaReporte())
                .build();
    }
}
