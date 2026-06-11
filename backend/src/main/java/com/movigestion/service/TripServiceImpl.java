package com.movigestion.service;

import com.movigestion.dto.request.CreateTripRequestDTO;
import com.movigestion.dto.request.UpdateTripStatusDTO;
import com.movigestion.dto.response.TripResponseDTO;
import com.movigestion.entity.*;
import com.movigestion.exception.BusinessRuleViolationException;
import com.movigestion.exception.InvalidStatusTransitionException;
import com.movigestion.exception.ResourceNotFoundException;
import com.movigestion.repository.TripRepository;
import com.movigestion.repository.UserRepository;
import com.movigestion.repository.VehicleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TripServiceImpl implements TripService {

    private final TripRepository tripRepository;
    private final UserRepository userRepository;
    private final VehicleRepository vehicleRepository;

    @Override
    @Transactional
    public TripResponseDTO createTrip(CreateTripRequestDTO request) {
        String currentUsername = SecurityContextHolder.getContext().getAuthentication().getName();
        User adminUser = userRepository.findByUsername(currentUsername)
                .orElseThrow(() -> new ResourceNotFoundException("Admin no encontrado"));

        User driver = null;
        Vehicle vehicle = null;
        TripStatusEnum initialState = TripStatusEnum.SIN_CONDUCTOR_ASIGNADO;

        if (request.getIdTransportista() != null && request.getIdVehiculo() != null) {
            driver = userRepository.findById(request.getIdTransportista())
                    .orElseThrow(() -> new ResourceNotFoundException("Conductor no encontrado"));
            vehicle = vehicleRepository.findById(request.getIdVehiculo())
                    .orElseThrow(() -> new ResourceNotFoundException("Vehículo no encontrado"));

            // BR-FLEET-01
            if (tripRepository.existsByIdTransportistaAndEstado(driver.getId(), TripStatusEnum.EN_CAMINO)) {
                throw new BusinessRuleViolationException("El conductor ya se encuentra en un viaje EN_CAMINO.");
            }
            if (tripRepository.existsByIdVehiculoAndEstado(vehicle.getIdVehiculo(), TripStatusEnum.EN_CAMINO)) {
                throw new BusinessRuleViolationException("El vehículo ya se encuentra en un viaje EN_CAMINO.");
            }

            // BR-FLEET-02
            if (vehicle.getEstado() == VehicleStatusEnum.EN_MANTENIMIENTO) {
                throw new BusinessRuleViolationException("El vehículo no puede ser asignado porque está en mantenimiento.");
            }
            LocalDate today = LocalDate.now();
            if (!vehicle.getFechaVencimientoSoat().isAfter(today) || !vehicle.getFechaVencimientoRevision().isAfter(today)) {
                throw new BusinessRuleViolationException("El vehículo no puede ser asignado porque tiene el SOAT o la Revisión Técnica vencidos.");
            }

            initialState = TripStatusEnum.ASIGNADO;
            vehicle.setEstado(VehicleStatusEnum.EN_RUTA);
            vehicleRepository.save(vehicle);
        }

        Trip newTrip = Trip.builder()
                .admin(adminUser)
                .driver(driver)
                .vehicle(vehicle)
                .destinoLatitud(request.getDestinoLatitud())
                .destinoLongitud(request.getDestinoLongitud())
                .estado(initialState)
                .build();

        Trip savedTrip = tripRepository.save(newTrip);
        return mapToDTO(savedTrip);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TripResponseDTO> findAllTrips() {
        return tripRepository.findAll().stream().map(this::mapToDTO).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<TripResponseDTO> getDriverTrips(Integer driverId, String scope) {
        if ("active".equalsIgnoreCase(scope)) {
            return tripRepository.findByIdTransportistaAndEstado(driverId, TripStatusEnum.EN_CAMINO)
                    .stream().map(this::mapToDTO).collect(Collectors.toList());
        } else if ("pending".equalsIgnoreCase(scope)) {
            return tripRepository.findByIdTransportistaAndEstado(driverId, TripStatusEnum.ASIGNADO)
                    .stream().map(this::mapToDTO).collect(Collectors.toList());
        }
        return tripRepository.findByIdTransportistaAndEstado(driverId, TripStatusEnum.EN_CAMINO)
                .stream().map(this::mapToDTO).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public TripResponseDTO updateTripStatus(Integer tripId, UpdateTripStatusDTO request) {
        Trip trip = tripRepository.findById(tripId)
                .orElseThrow(() -> new ResourceNotFoundException("Viaje no encontrado"));

        TripStatusEnum currentStatus = trip.getEstado();
        TripStatusEnum newStatus = request.getNuevoEstado();

        // BR-TRIP-01: Control de Máquina de Estados
        if (newStatus == TripStatusEnum.CANCELADO) {
            String currentUsername = SecurityContextHolder.getContext().getAuthentication().getName();
            User currentUser = userRepository.findByUsername(currentUsername).orElseThrow();
            if (currentUser.getRol() != Role.ADMIN) {
                throw new org.springframework.security.access.AccessDeniedException("Solo los administradores pueden cancelar un viaje.");
            }
            if (currentStatus == TripStatusEnum.COMPLETADO) {
                throw new InvalidStatusTransitionException("No se puede cancelar un viaje que ya está COMPLETADO.");
            }
        } else {
            if (currentStatus == TripStatusEnum.SIN_CONDUCTOR_ASIGNADO && newStatus != TripStatusEnum.ASIGNADO) {
                throw new InvalidStatusTransitionException("Transición de estado inválida.");
            }
            if (currentStatus == TripStatusEnum.ASIGNADO && newStatus != TripStatusEnum.EN_CAMINO) {
                throw new InvalidStatusTransitionException("Transición de estado inválida.");
            }
            if (currentStatus == TripStatusEnum.EN_CAMINO && newStatus != TripStatusEnum.COMPLETADO) {
                throw new InvalidStatusTransitionException("Transición de estado inválida.");
            }
            if (currentStatus == TripStatusEnum.COMPLETADO || currentStatus == TripStatusEnum.CANCELADO) {
                throw new InvalidStatusTransitionException("El viaje ya ha finalizado y no puede cambiar de estado.");
            }
        }

        trip.setEstado(newStatus);

        if (newStatus == TripStatusEnum.EN_CAMINO) {
            trip.setFechaInicio(LocalDateTime.now());
        }

        // BR-TRIP-02: Liberar recursos a estado DISPONIBLE
        if (newStatus == TripStatusEnum.COMPLETADO || newStatus == TripStatusEnum.CANCELADO) {
            trip.setFechaEntrega(LocalDateTime.now());
            if (trip.getVehicle() != null) {
                Vehicle vehicle = trip.getVehicle();
                vehicle.setEstado(VehicleStatusEnum.DISPONIBLE);
                vehicleRepository.save(vehicle);
            }
        }

        Trip savedTrip = tripRepository.save(trip);
        return mapToDTO(savedTrip);
    }

    private TripResponseDTO mapToDTO(Trip trip) {
        return TripResponseDTO.builder()
                .idEnvio(trip.getIdEnvio())
                .idAdministrador(trip.getAdmin() != null ? trip.getAdmin().getId() : null)
                .idTransportista(trip.getDriver() != null ? trip.getDriver().getId() : null)
                .idVehiculo(trip.getVehicle() != null ? trip.getVehicle().getIdVehiculo() : null)
                .destinoLatitud(trip.getDestinoLatitud())
                .destinoLongitud(trip.getDestinoLongitud())
                .estado(trip.getEstado().name())
                .fechaCreacion(trip.getFechaCreacion())
                .fechaInicio(trip.getFechaInicio())
                .fechaEntrega(trip.getFechaEntrega())
                .build();
    }
}
