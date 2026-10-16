package com.senai.lumiere.favoritos.dto;

import com.senai.lumiere.favoritos.model.TipoTitulo;

import java.time.Instant;

public record DesejoResponse(
        Long id,
        Long tituloId,
        TipoTitulo tipo,
        Instant criadoEm
) {
}
