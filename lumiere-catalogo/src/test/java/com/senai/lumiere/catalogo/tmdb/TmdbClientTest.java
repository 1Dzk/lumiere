package com.senai.lumiere.catalogo.tmdb;

import com.senai.lumiere.catalogo.exception.TituloNaoEncontradoException;
import com.senai.lumiere.catalogo.exception.TmdbIndisponivelException;
import com.senai.lumiere.catalogo.tmdb.dto.TmdbFilmeDetalhe;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class TmdbClientTest {

    private final RestClient.Builder builder = RestClient.builder().baseUrl("http://tmdb.teste");
    private final MockRestServiceServer servidor = MockRestServiceServer.bindTo(builder).build();
    private final TmdbClient tmdbClient = new TmdbClient(builder.build());

    @Test
    void detalheFilme_deveLancarTituloNaoEncontrado_quandoTmdbResponde404() {
        servidor.expect(requestTo("http://tmdb.teste/movie/999999?language=pt-BR"))
                .andRespond(withStatus(HttpStatus.NOT_FOUND));

        assertThatThrownBy(() -> tmdbClient.detalheFilme(999999L))
                .isInstanceOf(TituloNaoEncontradoException.class);
    }

    @Test
    void detalheFilme_deveLancarTmdbIndisponivel_quandoTmdbRespondeErroDeServidor() {
        servidor.expect(requestTo("http://tmdb.teste/movie/1?language=pt-BR"))
                .andRespond(withStatus(HttpStatus.SERVICE_UNAVAILABLE));

        assertThatThrownBy(() -> tmdbClient.detalheFilme(1L))
                .isInstanceOf(TmdbIndisponivelException.class);
    }

    @Test
    void detalheFilme_deveRetornarDadosQuandoSucesso() {
        servidor.expect(requestTo("http://tmdb.teste/movie/1?language=pt-BR"))
                .andRespond(withSuccess("{\"id\":1,\"title\":\"Duna\",\"runtime\":155}", MediaType.APPLICATION_JSON));

        TmdbFilmeDetalhe filme = tmdbClient.detalheFilme(1L);

        assertThat(filme.title()).isEqualTo("Duna");
        assertThat(filme.runtime()).isEqualTo(155);
    }
}
