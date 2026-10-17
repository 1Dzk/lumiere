package com.senai.lumiere.gateway.routing;

import com.senai.lumiere.gateway.exception.RotaNaoEncontradaException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class RotaResolver {

    private final String authUrl;
    private final String catalogoUrl;
    private final String favoritosUrl;
    private final String notificacoesUrl;

    public RotaResolver(
            @Value("${lumiere.auth.url}") String authUrl,
            @Value("${lumiere.catalogo.url}") String catalogoUrl,
            @Value("${lumiere.favoritos.url}") String favoritosUrl,
            @Value("${lumiere.notificacoes.url}") String notificacoesUrl
    ) {
        this.authUrl = authUrl;
        this.catalogoUrl = catalogoUrl;
        this.favoritosUrl = favoritosUrl;
        this.notificacoesUrl = notificacoesUrl;
    }

    public String resolver(String caminho) {
        if (caminho.startsWith("/auth")) {
            return authUrl;
        }
        if (caminho.startsWith("/catalogo")) {
            return catalogoUrl;
        }
        if (caminho.startsWith("/favoritos") || caminho.startsWith("/streamings-seguidos") || caminho.startsWith("/desejos")) {
            return favoritosUrl;
        }
        if (caminho.startsWith("/notificacoes")) {
            return notificacoesUrl;
        }

        throw new RotaNaoEncontradaException(caminho);
    }
}
