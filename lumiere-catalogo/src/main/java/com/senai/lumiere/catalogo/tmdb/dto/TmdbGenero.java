package com.senai.lumiere.catalogo.tmdb.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record TmdbGenero(
        Long id,
        String name
) {
}
