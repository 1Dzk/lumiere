package com.senai.lumiere.catalogo.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Um streaming que existe de verdade na TMDB, pra escolher ao seguir um streaming")
public record ProvedorDisponivelResponse(

        @Schema(example = "Netflix")
        String nome,

        @Schema(description = "URL pronta do logo do streaming", example = "https://image.tmdb.org/t/p/w500/pbpMk2JmcoNnQwx5JGpXngfoWtp.jpg")
        String logoUrl
) {
}
