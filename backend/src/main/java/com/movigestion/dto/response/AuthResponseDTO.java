package com.movigestion.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;

public class AuthResponseDTO {

    // Usamos @JsonProperty para que el JSON coincida con la interfaz de React
    @JsonProperty("id_usuario")
    private Integer idUsuario;

    private String usuario;
    private String rol;
    private String token;

    public AuthResponseDTO(Integer idUsuario, String usuario, String rol, String token) {
        this.idUsuario = idUsuario;
        this.usuario = usuario;
        this.rol = rol;
        this.token = token;
    }

    // Agrega los Getters y Setters correspondientes
    public Integer getIdUsuario() { return idUsuario; }
    public void setIdUsuario(Integer idUsuario) { this.idUsuario = idUsuario; }

    public String getUsuario() { return usuario; }
    public void setUsuario(String usuario) { this.usuario = usuario; }

    public String getRol() { return rol; }
    public void setRol(String rol) { this.rol = rol; }

    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }
}