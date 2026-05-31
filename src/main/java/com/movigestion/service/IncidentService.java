package com.movigestion.service;

import com.movigestion.dto.request.CreateIncidentRequestDTO;
import com.movigestion.dto.response.IncidentResponseDTO;

import java.util.List;

public interface IncidentService {
    IncidentResponseDTO createIncident(CreateIncidentRequestDTO request);
    List<IncidentResponseDTO> findAllIncidents();
    List<IncidentResponseDTO> getDriverIncidents();
}
