package com.movigestion.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.hibernate.validator.constraints.URL;
import lombok.Data;

@Data
public class CreateDriverRequestDTO {

    @NotBlank(message = "El DNI es obligatorio")
    @Size(min = 8, max = 8, message = "El DNI debe tener exactamente 8 caracteres")
    private String dni;

    @NotBlank(message = "El usuario es obligatorio")
    private String usuario;

    @NotBlank(message = "La contraseña es obligatoria")
    private String contrasena;

    @NotBlank(message = "El celular es obligatorio")
    @Size(min = 9, max = 15, message = "El número de celular debe tener entre 9 y 15 caracteres")
    private String celular;

    @URL(message = "La URL de la fotografía de perfil no tiene un formato válido")
    private String fotografiaPerfil;
}
