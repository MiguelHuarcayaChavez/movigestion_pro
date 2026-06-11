package com.movigestion.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RegisterAdminRequestDTO {
    @NotNull
    @Size(min = 8, max = 8)
    private String dni;
    @NotNull
    private String username;
    @NotNull
    private String password;
    @NotNull
    private String celular;
}
