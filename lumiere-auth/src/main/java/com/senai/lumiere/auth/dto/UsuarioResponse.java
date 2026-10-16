package com.senai.lumiere.auth.dto;

import java.time.Instant;

public record UsuarioResponse(
        Long id,
        String nome,
        String email,
        Instant criadoEm
) {
}
