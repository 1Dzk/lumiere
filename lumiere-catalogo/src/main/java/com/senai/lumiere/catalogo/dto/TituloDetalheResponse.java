package com.senai.lumiere.catalogo.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "Dados completos de um título. Campos de filme e de série se excluem: um filme traz `duracaoMinutos` e `numeroTemporadas` null, e vice-versa")
public record TituloDetalheResponse(

        @Schema(description = "Id na TMDB", example = "969681")
        Long id,

        @Schema(example = "FILME")
        TipoTitulo tipo,

        @Schema(description = "Título em português", example = "Homem-Aranha: Um Novo Dia")
        String titulo,

        @Schema(description = "Título original", example = "Spider-Man: Brand New Day")
        String tituloOriginal,

        @Schema(example = "É um novo dia para Peter Parker...")
        String sinopse,

        @Schema(example = "https://image.tmdb.org/t/p/w500/x0nvYzQpyJc5pdT9lMnkMuYAg0O.jpg")
        String posterUrl,

        @Schema(description = "Imagem larga, boa para banner", example = "https://image.tmdb.org/t/p/w500/subj7knTn4DeS6KS7qI66EQKe9i.jpg")
        String backdropUrl,

        @Schema(example = "2026-07-29")
        String dataLancamento,

        @Schema(example = "[\"Ficção científica\", \"Ação\", \"Aventura\"]")
        List<String> generos,

        @Schema(description = "Nota média da TMDB, de 0 a 10", example = "8.352")
        Double notaMedia,

        @Schema(description = "Duração em minutos — só para filmes", example = "143")
        Integer duracaoMinutos,

        @Schema(description = "Quantidade de temporadas — só para séries", example = "4")
        Integer numeroTemporadas
) {
}
