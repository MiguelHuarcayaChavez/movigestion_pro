package com.movigestion.dto.request;

import com.movigestion.entity.IncidentTypeEnum;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class CreateIncidentRequestDTO {

    @NotNull(message = "La clasificación de la incidencia es obligatoria")
    private IncidentTypeEnum clasificacion;

    @NotBlank(message = "La descripción es obligatoria")
    private String descripcion;

    @NotNull(message = "La latitud del reporte es obligatoria")
    private BigDecimal latitudReporte;

    @NotNull(message = "La longitud del reporte es obligatoria")
    private BigDecimal longitudReporte;
}
