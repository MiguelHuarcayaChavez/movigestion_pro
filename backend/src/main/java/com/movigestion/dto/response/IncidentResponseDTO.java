package com.movigestion.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.movigestion.entity.IncidentTypeEnum;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
public class IncidentResponseDTO {
    @JsonProperty("id_incidencia")
    private Integer idIncidencia;

    @JsonProperty("id_envio")
    private Integer idEnvio;

    @JsonProperty("id_vehiculo")
    private Integer idVehiculo;

    @JsonProperty("id_transportista")
    private Integer idTransportista;

    private IncidentTypeEnum clasificacion;
    private String descripcion;

    @JsonProperty("latitud_reporte")
    private BigDecimal latitudReporte;

    @JsonProperty("longitud_reporte")
    private BigDecimal longitudReporte;

    @JsonProperty("fecha_reporte")
    private LocalDateTime fechaReporte;
}
