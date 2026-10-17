package com.senai.lumiere.favoritos.dto;

import com.senai.lumiere.favoritos.model.TipoTitulo;

public record DesejoInternoResponse(
        Long id,
        Long usuarioId,
        Long tituloId,
        TipoTitulo tipo
) {
}
