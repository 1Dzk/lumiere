package com.senai.lumiere.favoritos.dto;

import com.senai.lumiere.favoritos.model.TipoTitulo;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

@Schema(description = "Título a ser favoritado")
public record FavoritoRequest(

        @Schema(description = "Id do título na TMDB, o mesmo devolvido pelo lumiere-catalogo", example = "969681")
        @NotNull(message = "tituloId é obrigatório")
        Long tituloId,

        @Schema(description = "Se o título é filme ou série", example = "FILME")
        @NotNull(message = "tipo é obrigatório")
        TipoTitulo tipo
) {
}
