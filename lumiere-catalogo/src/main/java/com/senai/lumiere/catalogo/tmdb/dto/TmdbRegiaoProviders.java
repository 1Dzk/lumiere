package com.senai.lumiere.catalogo.tmdb.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record TmdbRegiaoProviders(
        List<TmdbProvedor> flatrate,
        List<TmdbProvedor> rent,
        List<TmdbProvedor> buy
) {
}
