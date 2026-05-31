package com.movigestion.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class CreateTripRequestDTO {
    private Integer idTransportista;
    private Integer idVehiculo;

    @NotNull(message = "La latitud de destino es obligatoria")
    private BigDecimal destinoLatitud;

    @NotNull(message = "La longitud de destino es obligatoria")
    private BigDecimal destinoLongitud;
}
