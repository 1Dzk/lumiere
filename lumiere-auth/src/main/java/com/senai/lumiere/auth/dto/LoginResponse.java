package com.senai.lumiere.auth.dto;

public record LoginResponse(
        String token,
        String tipo,
        UsuarioResponse usuario
) {
    public LoginResponse(String token, UsuarioResponse usuario) {
        this(token, "Bearer", usuario);
    }
}
