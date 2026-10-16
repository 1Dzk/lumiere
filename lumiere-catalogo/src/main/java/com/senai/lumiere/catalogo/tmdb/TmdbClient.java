package com.senai.lumiere.catalogo.tmdb;

import com.senai.lumiere.catalogo.exception.TituloNaoEncontradoException;
import com.senai.lumiere.catalogo.exception.TmdbIndisponivelException;
import com.senai.lumiere.catalogo.tmdb.dto.TmdbFilmeDetalhe;
import com.senai.lumiere.catalogo.tmdb.dto.TmdbListaResponse;
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

    public TmdbListaResponse emAlta() {
        try {
            return restClient.get()
                    .uri(uriBuilder -> uriBuilder.path("/trending/all/day")
                            .queryParam("language", LANGUAGE)
                            .build())
                    .retrieve()
                    .body(TmdbListaResponse.class);
        } catch (RestClientException ex) {
            throw new TmdbIndisponivelException(ex);
        }
    }
}
