package com.movigestion.service;

import com.movigestion.dto.request.CreateDriverRequestDTO;
import com.movigestion.dto.response.DriverResponseDTO;

import java.util.List;

public interface DriverService {
    DriverResponseDTO createDriver(CreateDriverRequestDTO dto);
    List<DriverResponseDTO> findAllActiveDrivers();
    DriverResponseDTO findDriverById(Integer id);
}
