package com.senai.lumiere.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Resposta do login, com o token JWT e o usuário dono dele")
public record LoginResponse(

        @Schema(description = "Token JWT assinado, válido por 24h por padrão",
                example = "eyJhbGciOiJIUzM4NCJ9.eyJzdWIiOiJhbmFAbHVtaWVyZS5kZXYiLCJpZCI6MX0.abc123")
        String token,

        @Schema(description = "Tipo do token, para montar o cabeçalho `Authorization: Bearer <token>`", example = "Bearer")
        String tipo,

        UsuarioResponse usuario
) {
    public LoginResponse(String token, UsuarioResponse usuario) {
        this(token, "Bearer", usuario);
    }
}
