package com.senai.lumiere.favoritos.dto;

import com.senai.lumiere.favoritos.model.TipoTitulo;
import jakarta.validation.constraints.NotNull;

public record DesejoRequest(
        @NotNull(message = "tituloId é obrigatório")
        Long tituloId,

        @NotNull(message = "tipo é obrigatório")
        TipoTitulo tipo
) {
}
