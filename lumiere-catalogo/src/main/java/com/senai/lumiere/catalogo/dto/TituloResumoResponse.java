package com.senai.lumiere.catalogo.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Versão resumida de um título, usada em listas (busca e em alta)")
public record TituloResumoResponse(

        @Schema(description = "Id na TMDB — é o `tituloId` usado ao favoritar", example = "969681")
        Long id,

        @Schema(description = "Se é filme ou série", example = "FILME")
        TipoTitulo tipo,

        @Schema(example = "Homem-Aranha: Um Novo Dia")
        String titulo,

        @Schema(description = "URL pronta do pôster (null quando a TMDB não tem imagem)",
                example = "https://image.tmdb.org/t/p/w500/x0nvYzQpyJc5pdT9lMnkMuYAg0O.jpg")
        String posterUrl,

        @Schema(description = "Estreia do filme ou primeira exibição da série (AAAA-MM-DD)", example = "2026-07-29")
        String dataLancamento,

        @Schema(example = "É um novo dia para Peter Parker...")
        String sinopse
) {
}
