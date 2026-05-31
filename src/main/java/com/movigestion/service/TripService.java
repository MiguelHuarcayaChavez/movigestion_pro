package com.movigestion.service;

import com.movigestion.dto.request.CreateTripRequestDTO;
import com.movigestion.dto.request.UpdateTripStatusDTO;
import com.movigestion.dto.response.TripResponseDTO;

import java.util.List;

public interface TripService {
    TripResponseDTO createTrip(CreateTripRequestDTO request);
    List<TripResponseDTO> findAllTrips();
    List<TripResponseDTO> getDriverTrips(Integer driverId, String scope);
    TripResponseDTO updateTripStatus(Integer tripId, UpdateTripStatusDTO request);
}
