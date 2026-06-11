package com.movigestion.dto.request;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;

@Data
public class CreateVehicleRequestDTO {

    @NotBlank(message = "La placa es obligatoria")
    private String placa;

    @NotNull(message = "La fecha de vencimiento del SOAT es obligatoria")
    @Future(message = "La fecha de vencimiento del SOAT debe estar en el futuro")
    private LocalDate fechaVencimientoSoat;

    @NotNull(message = "La fecha de vencimiento de la revisión técnica es obligatoria")
    @Future(message = "La fecha de vencimiento de la revisión técnica debe estar en el futuro")
    private LocalDate fechaVencimientoRevisionTecnica;
}
