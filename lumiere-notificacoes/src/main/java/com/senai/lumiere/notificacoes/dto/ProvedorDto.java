package com.senai.lumiere.notificacoes.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record ProvedorDto(
        String nome
) {
}
