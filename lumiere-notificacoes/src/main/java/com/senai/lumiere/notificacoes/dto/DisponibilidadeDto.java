package com.senai.lumiere.notificacoes.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record DisponibilidadeDto(
        Long tituloId,
        boolean disponivelNaRegiaoSolicitada,
        List<ProvedorDto> streamingsNaRegiao
) {
}
