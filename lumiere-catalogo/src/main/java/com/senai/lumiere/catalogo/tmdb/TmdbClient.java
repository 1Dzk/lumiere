package com.senai.lumiere.catalogo.tmdb;

import com.senai.lumiere.catalogo.exception.TituloNaoEncontradoException;
import com.senai.lumiere.catalogo.exception.TmdbIndisponivelException;
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

@Component
@RequiredArgsConstructor
public class TmdbClient {

    private static final String LANGUAGE = "pt-BR";

    private final RestClient restClient;

    public TmdbListaResponse buscar(String titulo) {
        try {
            return restClient.get()
                    .uri(uriBuilder -> uriBuilder.path("/search/multi")
                            .queryParam("query", titulo)
                            .queryParam("language", LANGUAGE)
                            .queryParam("include_adult", false)
                            .build())
                    .retrieve()
                    .body(TmdbListaResponse.class);
        } catch (RestClientException ex) {
            throw new TmdbIndisponivelException(ex);
        }
    }

    public TmdbFilmeDetalhe detalheFilme(Long id) {
        try {
            return restClient.get()
                    .uri(uriBuilder -> uriBuilder.path("/movie/{id}")
                            .queryParam("language", LANGUAGE)
                            .build(id))
                    .retrieve()
                    .body(TmdbFilmeDetalhe.class);
        } catch (HttpClientErrorException.NotFound ex) {
            throw new TituloNaoEncontradoException(id);
        } catch (RestClientException ex) {
            throw new TmdbIndisponivelException(ex);
        }
    }

    public TmdbSerieDetalhe detalheSerie(Long id) {
        try {
            return restClient.get()
                    .uri(uriBuilder -> uriBuilder.path("/tv/{id}")
                            .queryParam("language", LANGUAGE)
                            .build(id))
                    .retrieve()
                    .body(TmdbSerieDetalhe.class);
        } catch (HttpClientErrorException.NotFound ex) {
            throw new TituloNaoEncontradoException(id);
        } catch (RestClientException ex) {
            throw new TmdbIndisponivelException(ex);
        }
    }

    public TmdbProvidersResponse providersFilme(Long id) {
        try {
            return restClient.get()
                    .uri("/movie/{id}/watch/providers", id)
                    .retrieve()
                    .body(TmdbProvidersResponse.class);
        } catch (HttpClientErrorException.NotFound ex) {
            throw new TituloNaoEncontradoException(id);
        } catch (RestClientException ex) {
            throw new TmdbIndisponivelException(ex);
        }
    }

    public TmdbProvidersResponse providersSerie(Long id) {
        try {
            return restClient.get()
                    .uri("/tv/{id}/watch/providers", id)
                    .retrieve()
                    .body(TmdbProvidersResponse.class);
        } catch (HttpClientErrorException.NotFound ex) {
            throw new TituloNaoEncontradoException(id);
        } catch (RestClientException ex) {
            throw new TmdbIndisponivelException(ex);
        }
    }

    public TmdbListaResponse emAlta(int pagina) {
        try {
            return restClient.get()
                    .uri(uriBuilder -> uriBuilder.path("/trending/all/day")
                            .queryParam("language", LANGUAGE)
                            .queryParam("page", pagina)
                            .build())
                    .retrieve()
                    .body(TmdbListaResponse.class);
        } catch (RestClientException ex) {
            throw new TmdbIndisponivelException(ex);
        }
    }

    public TmdbGenerosResponse generosFilme() {
        try {
            return restClient.get()
                    .uri(uriBuilder -> uriBuilder.path("/genre/movie/list")
                            .queryParam("language", LANGUAGE)
                            .build())
                    .retrieve()
                    .body(TmdbGenerosResponse.class);
        } catch (RestClientException ex) {
            throw new TmdbIndisponivelException(ex);
        }
    }

    public TmdbGenerosResponse generosSerie() {
        try {
            return restClient.get()
                    .uri(uriBuilder -> uriBuilder.path("/genre/tv/list")
                            .queryParam("language", LANGUAGE)
                            .build())
                    .retrieve()
                    .body(TmdbGenerosResponse.class);
        } catch (RestClientException ex) {
            throw new TmdbIndisponivelException(ex);
        }
    }

    public TmdbListaResponse descobrirFilmes(Long generoId, int pagina) {
        try {
            return restClient.get()
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
                    .body(TmdbListaResponse.class);
        } catch (RestClientException ex) {
            throw new TmdbIndisponivelException(ex);
        }
    }

    public TmdbProvedoresDisponiveisResponse provedoresFilme(String regiao) {
        try {
            return restClient.get()
                    .uri(uriBuilder -> uriBuilder.path("/watch/providers/movie")
                            .queryParam("language", LANGUAGE)
                            .queryParam("watch_region", regiao)
                            .build())
                    .retrieve()
                    .body(TmdbProvedoresDisponiveisResponse.class);
        } catch (RestClientException ex) {
            throw new TmdbIndisponivelException(ex);
        }
    }

    public TmdbProvedoresDisponiveisResponse provedoresSerie(String regiao) {
        try {
            return restClient.get()
                    .uri(uriBuilder -> uriBuilder.path("/watch/providers/tv")
                            .queryParam("language", LANGUAGE)
                            .queryParam("watch_region", regiao)
                            .build())
                    .retrieve()
                    .body(TmdbProvedoresDisponiveisResponse.class);
        } catch (RestClientException ex) {
            throw new TmdbIndisponivelException(ex);
        }
    }

    public TmdbListaResponse descobrirSeries(Long generoId, int pagina) {
        try {
            return restClient.get()
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
                    .body(TmdbListaResponse.class);
        } catch (RestClientException ex) {
            throw new TmdbIndisponivelException(ex);
        }
    }
}
