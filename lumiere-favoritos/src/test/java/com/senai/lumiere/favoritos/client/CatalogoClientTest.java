package com.senai.lumiere.favoritos.client;

import com.senai.lumiere.favoritos.model.TipoTitulo;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class CatalogoClientTest {

    private final RestClient.Builder builder = RestClient.builder().baseUrl("http://catalogo.teste");
    private final MockRestServiceServer servidor = MockRestServiceServer.bindTo(builder).build();
    private final CatalogoClient catalogoClient = new CatalogoClient(builder.build());

    @Test
    void tituloExiste_deveRetornarTrueQuandoCatalogoConfirma() {
        servidor.expect(requestTo("http://catalogo.teste/catalogo/filmes/1"))
                .andRespond(withSuccess());

        assertThat(catalogoClient.tituloExiste(1L, TipoTitulo.FILME)).isTrue();
    }

    @Test
    void tituloExiste_deveRetornarFalseQuandoCatalogoResponde404() {
        servidor.expect(requestTo("http://catalogo.teste/catalogo/filmes/999999999"))
                .andRespond(withStatus(HttpStatus.NOT_FOUND));

        assertThat(catalogoClient.tituloExiste(999999999L, TipoTitulo.FILME)).isFalse();
    }

    @Test
    void tituloExiste_deveDeixarPassarQuandoCatalogoEstaIndisponivel() {
        servidor.expect(requestTo("http://catalogo.teste/catalogo/series/1"))
                .andRespond(withStatus(HttpStatus.SERVICE_UNAVAILABLE));

        assertThat(catalogoClient.tituloExiste(1L, TipoTitulo.SERIE)).isTrue();
    }
}
