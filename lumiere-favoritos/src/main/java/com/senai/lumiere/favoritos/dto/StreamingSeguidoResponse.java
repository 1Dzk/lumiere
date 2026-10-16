package com.senai.lumiere.favoritos.dto;

import java.time.Instant;

public record StreamingSeguidoResponse(
        Long id,
        String nomeStreaming,
        Instant criadoEm
) {
}
