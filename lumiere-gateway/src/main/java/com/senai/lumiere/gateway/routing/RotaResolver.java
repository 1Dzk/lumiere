package com.senai.lumiere.gateway.routing;

import com.senai.lumiere.gateway.exception.RotaNaoEncontradaException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

/**
 * Tabela de rotas do gateway: qual prefixo de caminho vai para qual serviço. É a única fonte dessa
 * informação: o encaminhamento e a documentação (/docs) leem daqui, então nunca divergem.
 */
@Component
public class RotaResolver {

    /** Um serviço atrás do gateway e os prefixos de caminho que levam até ele. */
    public record Rota(String id, String rotulo, String descricao, List<String> prefixos, String url) {

        /** O prefixo precisa casar com um segmento inteiro: "/auth" atende "/auth" e "/auth/x", mas não "/authors". */
        boolean atende(String caminho) {
            return prefixos.stream().anyMatch(prefixo -> caminho.equals(prefixo) || caminho.startsWith(prefixo + "/"));
        }
    }

    private final List<Rota> rotas;

    public RotaResolver(
            @Value("${lumiere.auth.url}") String authUrl,
            @Value("${lumiere.catalogo.url}") String catalogoUrl,
            @Value("${lumiere.favoritos.url}") String favoritosUrl,
            @Value("${lumiere.notificacoes.url}") String notificacoesUrl
    ) {
        this.rotas = List.of(
                new Rota("auth", "Auth", "Cadastro, login e dados do usuário autenticado",
                        List.of("/auth"), authUrl),
                new Rota("catalogo", "Catálogo", "Busca de filmes e séries e onde assistir",
                        List.of("/catalogo"), catalogoUrl),
                new Rota("favoritos", "Favoritos", "Favoritos, lista de desejos e streamings seguidos de cada pessoa",
                        List.of("/favoritos", "/desejos", "/streamings-seguidos"), favoritosUrl),
                new Rota("notificacoes", "Notificações", "Avisos de títulos desejados que chegaram a algum streaming",
                        List.of("/notificacoes"), notificacoesUrl)
        );
    }

    public String resolver(String caminho) {
        return rotas.stream()
                .filter(rota -> rota.atende(caminho))
                .findFirst()
                .map(Rota::url)
                .orElseThrow(() -> new RotaNaoEncontradaException(caminho));
    }

    /** O gateway repassa este caminho para algum serviço? (Endpoints internos, como /interno, ficam de fora.) */
    public boolean expoe(String caminho) {
        return rotas.stream().anyMatch(rota -> rota.atende(caminho));
    }

    public List<Rota> rotas() {
        return rotas;
    }

    public Optional<Rota> porId(String id) {
        return rotas.stream().filter(rota -> rota.id().equals(id)).findFirst();
    }
}
