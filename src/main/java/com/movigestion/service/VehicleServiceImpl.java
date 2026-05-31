package com.movigestion.service;

import com.movigestion.dto.request.CreateVehicleRequestDTO;
import com.movigestion.dto.request.UpdateVehicleRequestDTO;
import com.movigestion.dto.response.VehicleResponseDTO;
import com.movigestion.entity.Vehicle;
import com.movigestion.entity.VehicleStatusEnum;
import com.movigestion.exception.BusinessRuleViolationException;
import com.movigestion.exception.ResourceConflictException;
import com.movigestion.exception.ResourceNotFoundException;
import com.movigestion.repository.VehicleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class VehicleServiceImpl implements VehicleService {

    private final VehicleRepository vehicleRepository;

    @Override
    @Transactional
    public VehicleResponseDTO create(CreateVehicleRequestDTO request) {
        if (vehicleRepository.existsByPlaca(request.getPlaca())) {
            throw new ResourceConflictException("La placa ingresada ya se encuentra registrada en el sistema.");
        }

        Vehicle vehicle = Vehicle.builder()
                .placa(request.getPlaca())
                .estado(VehicleStatusEnum.DISPONIBLE)
                .fechaVencimientoSoat(request.getFechaVencimientoSoat())
                .fechaVencimientoRevision(request.getFechaVencimientoRevisionTecnica())
                .build();

        Vehicle savedVehicle = vehicleRepository.save(vehicle);
        return mapToResponseDTO(savedVehicle);
    }

    @Override
    @Transactional(readOnly = true)
    public List<VehicleResponseDTO> findAll(VehicleStatusEnum estado) {
        List<Vehicle> vehicles;
        if (estado != null) {
            vehicles = vehicleRepository.findByEstado(estado);
        } else {
            vehicles = vehicleRepository.findAll();
        }
        return vehicles.stream().map(this::mapToResponseDTO).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public VehicleResponseDTO update(Integer id, UpdateVehicleRequestDTO request) {
        Vehicle vehicle = vehicleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Vehículo con ID " + id + " no encontrado."));

        if (vehicle.getEstado() == VehicleStatusEnum.EN_RUTA) {
            throw new BusinessRuleViolationException("No se puede modificar un vehículo que actualmente se encuentra en ruta.");
        }

        vehicle.setEstado(request.getEstado());
        vehicle.setFechaVencimientoSoat(request.getFechaVencimientoSoat());
        vehicle.setFechaVencimientoRevision(request.getFechaVencimientoRevisionTecnica());

        Vehicle updatedVehicle = vehicleRepository.save(vehicle);
        return mapToResponseDTO(updatedVehicle);
    }

    @Override
    @Transactional
    public void delete(Integer id) {
        Vehicle vehicle = vehicleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Vehículo con ID " + id + " no encontrado."));

        if (vehicle.getEstado() == VehicleStatusEnum.EN_RUTA) {
            throw new BusinessRuleViolationException("No se puede dar de baja un vehículo que actualmente se encuentra en ruta.");
        }

        vehicle.setEstado(VehicleStatusEnum.DE_BAJA);
        vehicleRepository.save(vehicle);
    }

    private VehicleResponseDTO mapToResponseDTO(Vehicle vehicle) {
        return VehicleResponseDTO.builder()
                .idVehiculo(vehicle.getIdVehiculo())
                .placa(vehicle.getPlaca())
                .estado(vehicle.getEstado())
                .fechaVencimientoSoat(vehicle.getFechaVencimientoSoat())
                .fechaVencimientoRevision(vehicle.getFechaVencimientoRevision())
                .build();
    }
}
