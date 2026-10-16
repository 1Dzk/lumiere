package com.senai.lumiere.catalogo.tmdb.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public record TmdbProvedor(
        @JsonProperty("provider_name") String providerName,
        @JsonProperty("logo_path") String logoPath
) {
}
