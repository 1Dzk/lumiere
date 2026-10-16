package com.senai.lumiere.catalogo.dto;

import java.util.List;

public record DisponibilidadeStreamingResponse(
        Long tituloId,
        String regiaoSolicitada,
        boolean disponivelNaRegiaoSolicitada,
        List<ProvedorStreaming> streamingsNaRegiao,
        List<DisponibilidadePorRegiao> outrasRegioes
) {
}
