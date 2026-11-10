package com.senai.lumiere.catalogo.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "Streamings de um título em um país específico")
public record DisponibilidadePorRegiao(

        @Schema(description = "País no formato ISO 3166-1", example = "US")
        String regiao,

        List<ProvedorStreaming> streamings,

        @Schema(description = "Página da TMDB com os streamings dessa região clicáveis (não é um link direto pro app do streaming)",
                example = "https://www.themoviedb.org/movie/969681-title/watch?locale=US")
        String link
) {
}
