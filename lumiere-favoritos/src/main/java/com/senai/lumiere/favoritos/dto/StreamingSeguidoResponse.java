package com.senai.lumiere.favoritos.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;

@Schema(description = "Um streaming seguido pelo usuário logado")
public record StreamingSeguidoResponse(

        @Schema(description = "Id do registro — é esse id que se usa para deixar de seguir", example = "1")
        Long id,

        @Schema(example = "Netflix")
        String nomeStreaming,

        @Schema(description = "Quando o usuário passou a seguir", example = "2026-11-03T18:25:43.511Z")
        Instant criadoEm
) {
}
