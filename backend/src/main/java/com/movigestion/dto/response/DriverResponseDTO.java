package com.movigestion.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.movigestion.entity.RoleEnum;
import com.movigestion.entity.UserStatusEnum;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DriverResponseDTO {

    @JsonProperty("id_usuario")
    private Integer idUsuario;

    private String dni;

    private String usuario;

    private String celular;

    @JsonProperty("fotografia_perfil")
    private String fotografiaPerfil;

    private RoleEnum rol;

    private UserStatusEnum estado;

    @JsonProperty("id_admin_creador")
    private Integer idAdminCreador;

    @JsonProperty("fecha_creacion")
    private LocalDateTime fechaCreacion;
}
