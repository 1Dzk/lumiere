package com.senai.lumiere.catalogo.dto;

public record TituloResumoResponse(
        Long id,
        TipoTitulo tipo,
        String titulo,
        String posterUrl,
        String dataLancamento,
        String sinopse
) {
}
