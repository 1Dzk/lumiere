package com.senai.lumiere.catalogo.tmdb;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestClient;

@Configuration
public class TmdbConfig {

    @Bean
    public RestClient tmdbRestClient(
            @Value("${tmdb.api.base-url}") String baseUrl,
            @Value("${tmdb.api.token}") String token
    ) {
        return RestClient.builder()
                .baseUrl(baseUrl)
                .defaultHeader("Authorization", "Bearer " + token)
                .defaultHeader("Accept", MediaType.APPLICATION_JSON_VALUE)
                .build();
    }
}
