package com.senai.lumiere.catalogo.exception;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;
import java.util.Map;

@Schema(name = "Erro", description = "Formato padrão de erro devolvido por todos os endpoints deste serviço")
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ErroResponse(

        @Schema(description = "Momento em que o erro aconteceu", example = "2026-11-03T18:25:43.511Z")
        Instant timestamp,

        @Schema(description = "Código HTTP da resposta", example = "404")
        int status,

        @Schema(description = "Explicação do que deu errado", example = "Título não encontrado: 999999999")
        String mensagem,

        @Schema(description = "Só vem em erro de validação (400): campo que falhou -> motivo")
        Map<String, String> erros
) {
}
