package com.senai.lumiere.catalogo.dto;

import java.util.List;

public record TituloDetalheResponse(
        Long id,
        TipoTitulo tipo,
        String titulo,
        String tituloOriginal,
        String sinopse,
        String posterUrl,
        String backdropUrl,
        String dataLancamento,
        List<String> generos,
        Double notaMedia,
        Integer duracaoMinutos,
        Integer numeroTemporadas
) {
}
