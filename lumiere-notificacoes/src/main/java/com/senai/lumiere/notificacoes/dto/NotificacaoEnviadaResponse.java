package com.senai.lumiere.notificacoes.dto;

import com.senai.lumiere.notificacoes.model.TipoTitulo;

import java.time.Instant;

public record NotificacaoEnviadaResponse(
        Long id,
        Long usuarioId,
        Long tituloId,
        TipoTitulo tipo,
        Instant enviadaEm
) {
}
