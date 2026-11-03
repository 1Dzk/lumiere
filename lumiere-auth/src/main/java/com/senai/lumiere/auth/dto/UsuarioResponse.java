package com.senai.lumiere.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;

@Schema(description = "Dados públicos do usuário (a senha nunca é devolvida)")
public record UsuarioResponse(

        @Schema(description = "Id do usuário, usado como dono dos favoritos e desejos", example = "1")
        Long id,

        @Schema(example = "Ana Souza")
        String nome,

        @Schema(example = "ana@lumiere.dev")
        String email,

        @Schema(description = "Quando a conta foi criada", example = "2026-11-03T18:25:43.511Z")
        Instant criadoEm
) {
}
