package com.senai.lumiere.catalogo.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI lumiereCatalogoOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("Lumiere — Catálogo")
                        .version("v1")
                        .description("""
                                Busca de filmes e séries e em quais streamings cada título está disponível.

                                Todos os endpoints são **públicos**: navegar o catálogo não exige conta. \
                                Os dados vêm da API da TMDB; este serviço não tem banco próprio, só um \
                                cache em memória (15 min) para não estourar o limite de chamadas da TMDB.

                                Os ids usados aqui são os ids da TMDB — são os mesmos que o \
                                `lumiere-favoritos` guarda quando você favorita um título.
                                """));
    }
}
