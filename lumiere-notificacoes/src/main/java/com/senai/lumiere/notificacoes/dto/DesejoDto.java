package com.senai.lumiere.notificacoes.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.senai.lumiere.notificacoes.model.TipoTitulo;

@JsonIgnoreProperties(ignoreUnknown = true)
public record DesejoDto(
        Long id,
        Long usuarioId,
        Long tituloId,
        TipoTitulo tipo
) {
}
