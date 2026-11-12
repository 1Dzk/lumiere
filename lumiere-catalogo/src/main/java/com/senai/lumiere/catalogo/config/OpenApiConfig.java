package com.senai.lumiere.catalogo.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Paths;
import io.swagger.v3.oas.models.info.Info;
import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {

    /**
     * O springdoc não garante ordem entre endpoints (depende da reflexão da JVM). Esta é a ordem de
     * leitura da documentação: do mais usado ao mais específico. Endpoint novo que não esteja aqui
     * entra no final.
     */
    private static final List<String> ORDEM_DOS_ENDPOINTS = List.of(
            "/catalogo/busca",
            "/catalogo/em-alta",
            "/catalogo/descobrir",
            "/catalogo/generos",
            "/catalogo/provedores",
            "/catalogo/filmes/{id}",
            "/catalogo/filmes/{id}/streamings",
            "/catalogo/series/{id}",
            "/catalogo/series/{id}/streamings"
    );

    @Bean
    public OpenApiCustomizer ordenarEndpoints() {
        return openApi -> {
            Paths original = openApi.getPaths();
            if (original == null) {
                return;
            }
            Paths ordenado = new Paths();
            ORDEM_DOS_ENDPOINTS.stream()
                    .filter(original::containsKey)
                    .forEach(caminho -> ordenado.addPathItem(caminho, original.get(caminho)));
            original.forEach((caminho, item) -> {
                if (!ordenado.containsKey(caminho)) {
                    ordenado.addPathItem(caminho, item);
                }
            });
            openApi.setPaths(ordenado);
        };
    }

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

                                ## Como os erros funcionam

                                Toda falha volta no mesmo formato (o modelo `Erro`): `timestamp`, `status`, \
                                `mensagem` e, nos erros de validação, `erros` com campo → motivo.

                                - `400` — parâmetro inválido (id menor que 1, página fora de 1–500, região fora do padrão...); o campo `erros` diz qual e por quê
                                - `404` — o título não existe na TMDB
                                - `405` — método HTTP não permitido para o endereço
                                - `500` — erro inesperado do serviço; o detalhe fica só no log
                                - `502` — a TMDB recusou a credencial do serviço (é configuração nossa, não erro seu)
                                - `503` — a TMDB está fora do ar ou o limite de chamadas dela foi excedido; tente de novo em instantes
                                """));
    }
}
