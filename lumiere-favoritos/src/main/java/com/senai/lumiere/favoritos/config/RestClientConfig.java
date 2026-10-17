package com.senai.lumiere.favoritos.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
public class RestClientConfig {

    @Bean
    public RestClient catalogoRestClient(@Value("${catalogo.url}") String catalogoUrl) {
        return RestClient.builder()
                .baseUrl(catalogoUrl)
                .build();
    }
}
