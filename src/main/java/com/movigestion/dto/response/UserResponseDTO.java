package com.movigestion.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserResponseDTO {
    private Integer id;
    private String dni;
    private String username;
    private String celular;
    private String rol;
    private String profilePicture;
    private String estado;
    private LocalDateTime creationDate;
}
