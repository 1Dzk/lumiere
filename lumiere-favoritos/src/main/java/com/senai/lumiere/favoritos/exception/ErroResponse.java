package com.senai.lumiere.favoritos.exception;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;
import java.util.Map;

@Schema(name = "Erro", description = "Formato padrão de erro devolvido por todos os endpoints deste serviço")
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ErroResponse(

        @Schema(description = "Momento em que o erro aconteceu", example = "2026-11-03T18:25:43.511Z")
        Instant timestamp,

        @Schema(description = "Código HTTP da resposta", example = "409")
        int status,

        @Schema(description = "Explicação do que deu errado", example = "Título já favoritado")
        String mensagem,

        @Schema(description = "Só vem em erro de validação (400): campo que falhou -> motivo",
                example = "{\"tituloId\": \"tituloId é obrigatório\"}")
        Map<String, String> erros
) {
}
