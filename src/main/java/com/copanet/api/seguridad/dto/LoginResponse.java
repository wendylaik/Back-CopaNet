package com.copanet.api.seguridad.dto;

import java.util.List;

public class LoginResponse {
    private Integer usuarioId;
    private String email;
    private String nombre;
    private List<String> roles;

    public LoginResponse(Integer usuarioId, String email, String nombre, List<String> roles) {
        this.usuarioId = usuarioId;
        this.email = email;
        this.nombre = nombre;
        this.roles = roles;
    }

    public Integer getUsuarioId() { return usuarioId; }
    public String getEmail() { return email; }
    public String getNombre() { return nombre; }
    public List<String> getRoles() { return roles; }
}
