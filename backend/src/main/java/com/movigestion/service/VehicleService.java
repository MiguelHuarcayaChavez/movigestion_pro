package com.movigestion.service;

import com.movigestion.dto.request.CreateVehicleRequestDTO;
import com.movigestion.dto.request.UpdateVehicleRequestDTO;
import com.movigestion.dto.response.VehicleResponseDTO;
import com.movigestion.entity.VehicleStatusEnum;

import java.util.List;

public interface VehicleService {
    VehicleResponseDTO create(CreateVehicleRequestDTO request);
    List<VehicleResponseDTO> findAll(VehicleStatusEnum estado);
    VehicleResponseDTO update(Integer id, UpdateVehicleRequestDTO request);
    void delete(Integer id);
}
