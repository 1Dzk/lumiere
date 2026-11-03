package com.senai.lumiere.notificacoes.dto;

import com.senai.lumiere.notificacoes.model.TipoTitulo;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;

@Schema(description = "Registro de um aviso já disparado")
public record NotificacaoEnviadaResponse(

        @Schema(description = "Id do registro", example = "1")
        Long id,

        @Schema(description = "Usuário que foi avisado", example = "1")
        Long usuarioId,

        @Schema(description = "Id do título na TMDB que ficou disponível", example = "969681")
        Long tituloId,

        @Schema(example = "FILME")
        TipoTitulo tipo,

        @Schema(description = "Quando o aviso foi disparado", example = "2026-11-03T18:25:43.511Z")
        Instant enviadaEm
) {
}
