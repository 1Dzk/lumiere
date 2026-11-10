package com.senai.lumiere.catalogo.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Gênero disponível para filtrar o catálogo")
public record GeneroResponse(

        @Schema(description = "Id do gênero na TMDB — usar no filtro `genero` de /catalogo/descobrir", example = "28")
        Long id,

        @Schema(example = "Ação")
        String nome
) {
}
