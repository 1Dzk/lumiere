package com.senai.lumiere.favoritos.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "Streaming que o usuário quer acompanhar")
public record StreamingSeguidoRequest(

        @Schema(description = "Nome do streaming, como aparece no catálogo", example = "Netflix")
        @NotBlank(message = "nomeStreaming é obrigatório")
        String nomeStreaming
) {
}
