package com.movigestion.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
public class TripResponseDTO {
    @JsonProperty("id_envio")
    private Integer idEnvio;

    @JsonProperty("id_administrador")
    private Integer idAdministrador;

    @JsonProperty("id_transportista")
    private Integer idTransportista;

    @JsonProperty("id_vehiculo")
    private Integer idVehiculo;

    @JsonProperty("destino_latitud")
    private BigDecimal destinoLatitud;

    @JsonProperty("destino_longitud")
    private BigDecimal destinoLongitud;

    private String estado;

    @JsonProperty("fecha_creacion")
    private LocalDateTime fechaCreacion;

    @JsonProperty("fecha_inicio")
    private LocalDateTime fechaInicio;

    @JsonProperty("fecha_entrega")
    private LocalDateTime fechaEntrega;
}
