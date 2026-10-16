package com.senai.lumiere.catalogo.tmdb.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record TmdbListaResponse(
        List<TmdbItemResumo> results
) {
}
