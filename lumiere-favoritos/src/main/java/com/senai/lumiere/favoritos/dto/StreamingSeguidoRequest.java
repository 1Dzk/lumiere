package com.senai.lumiere.favoritos.dto;

import jakarta.validation.constraints.NotBlank;

public record StreamingSeguidoRequest(
        @NotBlank(message = "nomeStreaming é obrigatório")
        String nomeStreaming
) {
}
