package com.senai.lumiere.catalogo.dto;

import java.util.List;

public record DisponibilidadePorRegiao(
        String regiao,
        List<ProvedorStreaming> streamings
) {
}
