package com.senai.lumiere.favoritos.dto;

import com.senai.lumiere.favoritos.model.TipoTitulo;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;

@Schema(description = "Um título na lista de desejos do usuário logado")
public record DesejoResponse(

        @Schema(description = "Id do desejo — é esse id que se usa para remover", example = "1")
        Long id,

        @Schema(description = "Id do título na TMDB", example = "969681")
        Long tituloId,

        @Schema(example = "FILME")
        TipoTitulo tipo,

        @Schema(description = "Quando o título entrou na lista", example = "2026-11-03T18:25:43.511Z")
        Instant criadoEm
) {
}
