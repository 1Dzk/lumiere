package com.senai.lumiere.catalogo.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Um streaming onde o título está disponível")
public record ProvedorStreaming(

        @Schema(example = "Netflix")
        String nome,

        @Schema(description = "URL pronta do logo do streaming", example = "https://image.tmdb.org/t/p/w500/pbpMk2JmcoNnQwx5JGpXngfoWtp.jpg")
        String logoUrl,

        @Schema(description = "Como o título está disponível nesse streaming", example = "ASSINATURA")
        TipoAcesso tipoAcesso
) {
}
