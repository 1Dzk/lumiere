package com.senai.lumiere.catalogo.tmdb.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record TmdbSerieDetalhe(
        Long id,
        String name,
        @JsonProperty("original_name") String originalName,
        String overview,
        @JsonProperty("poster_path") String posterPath,
        @JsonProperty("backdrop_path") String backdropPath,
        @JsonProperty("first_air_date") String firstAirDate,
        @JsonProperty("number_of_seasons") Integer numberOfSeasons,
        @JsonProperty("vote_average") Double voteAverage,
        List<TmdbGenero> genres
) {
}
