package com.senai.lumiere.catalogo.tmdb.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.Map;

@JsonIgnoreProperties(ignoreUnknown = true)
public record TmdbProvidersResponse(
        Long id,
        Map<String, TmdbRegiaoProviders> results
) {
}
