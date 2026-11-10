package com.senai.lumiere.catalogo.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "Onde assistir um título. Se não houver streaming na região pedida, `outrasRegioes` mostra em que países ele já está")
public record DisponibilidadeStreamingResponse(

        @Schema(description = "Id do título na TMDB", example = "969681")
        Long tituloId,

        @Schema(description = "País que foi consultado", example = "BR")
        String regiaoSolicitada,

        @Schema(description = "true quando há pelo menos um streaming na região pedida", example = "true")
        boolean disponivelNaRegiaoSolicitada,

        @Schema(description = "Streamings na região pedida; vazio quando o título não está disponível lá")
        List<ProvedorStreaming> streamingsNaRegiao,

        @Schema(description = "Preenchido só quando não há disponibilidade na região pedida: outros países onde o título está")
        List<DisponibilidadePorRegiao> outrasRegioes,

        @Schema(description = "Página da TMDB com os streamings da região pedida clicáveis (não é um link direto pro app do streaming — a TMDB não expõe isso de graça)",
                example = "https://www.themoviedb.org/movie/969681-title/watch?locale=BR")
        String linkAssistir
) {
}
