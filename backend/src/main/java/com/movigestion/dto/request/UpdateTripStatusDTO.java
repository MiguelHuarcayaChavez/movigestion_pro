package com.movigestion.dto.request;

import com.movigestion.entity.TripStatusEnum;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UpdateTripStatusDTO {
    @NotNull(message = "El nuevo estado es obligatorio")
    private TripStatusEnum nuevoEstado;
}
