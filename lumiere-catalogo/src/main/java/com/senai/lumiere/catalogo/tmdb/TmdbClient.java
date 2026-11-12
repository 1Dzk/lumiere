package com.senai.lumiere.catalogo.tmdb;

import com.senai.lumiere.catalogo.exception.TituloNaoEncontradoException;
import com.senai.lumiere.catalogo.exception.TmdbAcessoNegadoException;
import com.senai.lumiere.catalogo.exception.TmdbIndisponivelException;
import com.senai.lumiere.catalogo.exception.TmdbLimiteExcedidoException;
import com.senai.lumiere.catalogo.tmdb.dto.TmdbFilmeDetalhe;
import com.senai.lumiere.catalogo.tmdb.dto.TmdbGenerosResponse;
import com.senai.lumiere.catalogo.tmdb.dto.TmdbListaResponse;
import com.senai.lumiere.catalogo.tmdb.dto.TmdbProvedoresDisponiveisResponse;
import com.senai.lumiere.catalogo.tmdb.dto.TmdbProvidersResponse;
import com.senai.lumiere.catalogo.tmdb.dto.TmdbSerieDetalhe;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.util.function.Supplier;

/**
 * Única classe que fala com a TMDB. Também é a borda onde o erro do mundo externo (status HTTP, rede)
 * é traduzido para as exceções do nosso domínio: quem está acima daqui nunca vê um
 * {@link RestClientException}.
 */
@Component
@RequiredArgsConstructor
public class TmdbClient {

    private static final String LANGUAGE = "pt-BR";

    private final RestClient restClient;

    public TmdbListaResponse buscar(String titulo) {
        return chamar(null, () -> restClient.get()
                .uri(uriBuilder -> uriBuilder.path("/search/multi")
                        .queryParam("query", titulo)
                        .queryParam("language", LANGUAGE)
                        .queryParam("include_adult", false)
                        .build())
                .retrieve()
                .body(TmdbListaResponse.class));
    }

    public TmdbFilmeDetalhe detalheFilme(Long id) {
        return chamar(id, () -> restClient.get()
                .uri(uriBuilder -> uriBuilder.path("/movie/{id}")
                        .queryParam("language", LANGUAGE)
                        .build(id))
                .retrieve()
                .body(TmdbFilmeDetalhe.class));
    }

    public TmdbSerieDetalhe detalheSerie(Long id) {
        return chamar(id, () -> restClient.get()
                .uri(uriBuilder -> uriBuilder.path("/tv/{id}")
                        .queryParam("language", LANGUAGE)
                        .build(id))
                .retrieve()
                .body(TmdbSerieDetalhe.class));
    }

    public TmdbProvidersResponse providersFilme(Long id) {
        return chamar(id, () -> restClient.get()
                .uri("/movie/{id}/watch/providers", id)
                .retrieve()
                .body(TmdbProvidersResponse.class));
    }

    public TmdbProvidersResponse providersSerie(Long id) {
        return chamar(id, () -> restClient.get()
                .uri("/tv/{id}/watch/providers", id)
                .retrieve()
                .body(TmdbProvidersResponse.class));
    }

    public TmdbListaResponse emAlta(int pagina) {
        return chamar(null, () -> restClient.get()
                .uri(uriBuilder -> uriBuilder.path("/trending/all/day")
                        .queryParam("language", LANGUAGE)
                        .queryParam("page", pagina)
                        .build())
                .retrieve()
                .body(TmdbListaResponse.class));
    }

    public TmdbGenerosResponse generosFilme() {
        return chamar(null, () -> restClient.get()
                .uri(uriBuilder -> uriBuilder.path("/genre/movie/list")
                        .queryParam("language", LANGUAGE)
                        .build())
                .retrieve()
                .body(TmdbGenerosResponse.class));
    }

    public TmdbGenerosResponse generosSerie() {
        return chamar(null, () -> restClient.get()
                .uri(uriBuilder -> uriBuilder.path("/genre/tv/list")
                        .queryParam("language", LANGUAGE)
                        .build())
                .retrieve()
                .body(TmdbGenerosResponse.class));
    }

    public TmdbListaResponse descobrirFilmes(Long generoId, int pagina) {
        return chamar(null, () -> restClient.get()
                .uri(uriBuilder -> {
                    uriBuilder.path("/discover/movie")
                            .queryParam("language", LANGUAGE)
                            .queryParam("sort_by", "popularity.desc")
                            .queryParam("include_adult", false)
                            .queryParam("page", pagina);
                    if (generoId != null) {
                        uriBuilder.queryParam("with_genres", generoId);
                    }
                    return uriBuilder.build();
                })
                .retrieve()
                .body(TmdbListaResponse.class));
    }

    public TmdbListaResponse descobrirSeries(Long generoId, int pagina) {
        return chamar(null, () -> restClient.get()
                .uri(uriBuilder -> {
                    uriBuilder.path("/discover/tv")
                            .queryParam("language", LANGUAGE)
                            .queryParam("sort_by", "popularity.desc")
                            .queryParam("page", pagina);
                    if (generoId != null) {
                        uriBuilder.queryParam("with_genres", generoId);
                    }
                    return uriBuilder.build();
                })
                .retrieve()
                .body(TmdbListaResponse.class));
    }

    public TmdbProvedoresDisponiveisResponse provedoresFilme(String regiao) {
        return chamar(null, () -> restClient.get()
                .uri(uriBuilder -> uriBuilder.path("/watch/providers/movie")
                        .queryParam("language", LANGUAGE)
                        .queryParam("watch_region", regiao)
                        .build())
                .retrieve()
                .body(TmdbProvedoresDisponiveisResponse.class));
    }

    public TmdbProvedoresDisponiveisResponse provedoresSerie(String regiao) {
        return chamar(null, () -> restClient.get()
                .uri(uriBuilder -> uriBuilder.path("/watch/providers/tv")
                        .queryParam("language", LANGUAGE)
                        .queryParam("watch_region", regiao)
                        .build())
                .retrieve()
                .body(TmdbProvedoresDisponiveisResponse.class));
    }

    /**
     * Executa a chamada e traduz qualquer falha numa exceção de domínio. {@code tituloId} só é informado
     * nas chamadas de um título específico: é o único caso em que um 404 da TMDB significa "esse título não existe".
     */
    private <T> T chamar(Long tituloId, Supplier<T> chamada) {
        try {
            return chamada.get();
        } catch (RestClientException ex) {
            throw traduzir(ex, tituloId);
        }
    }

    private RuntimeException traduzir(RestClientException ex, Long tituloId) {
        if (ex instanceof HttpClientErrorException erroHttp) {
            int status = erroHttp.getStatusCode().value();
            if (status == 404 && tituloId != null) {
                return new TituloNaoEncontradoException(tituloId);
            }
            if (status == 401 || status == 403) {
                return new TmdbAcessoNegadoException(ex);
            }
            if (status == 429) {
                return new TmdbLimiteExcedidoException(ex);
            }
        }
        // 5xx da TMDB, timeout, rede caída ou qualquer outro 4xx inesperado
        return new TmdbIndisponivelException(ex);
    }
}
