package com.movigestion.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.movigestion.entity.VehicleStatusEnum;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VehicleResponseDTO {

    @JsonProperty("id_vehiculo")
    private Integer idVehiculo;

    private String placa;

    private VehicleStatusEnum estado;

    @JsonProperty("fecha_vencimiento_soat")
    private LocalDate fechaVencimientoSoat;

    @JsonProperty("fecha_vencimiento_revision")
    private LocalDate fechaVencimientoRevision;
}
