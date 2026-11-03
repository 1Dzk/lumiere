package com.senai.lumiere.favoritos.dto;

import com.senai.lumiere.favoritos.model.TipoTitulo;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Desejo com o dono explícito, usado só na chamada interna do lumiere-notificacoes")
public record DesejoInternoResponse(

        @Schema(description = "Id do desejo", example = "1")
        Long id,

        @Schema(description = "Id do usuário dono do desejo — é para ele que o aviso vai", example = "1")
        Long usuarioId,

        @Schema(description = "Id do título na TMDB", example = "969681")
        Long tituloId,

        @Schema(example = "FILME")
        TipoTitulo tipo
) {
}
