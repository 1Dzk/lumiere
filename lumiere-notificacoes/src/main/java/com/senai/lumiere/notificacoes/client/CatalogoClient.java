package com.senai.lumiere.notificacoes.client;

import com.senai.lumiere.notificacoes.dto.DisponibilidadeDto;
import com.senai.lumiere.notificacoes.model.TipoTitulo;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class CatalogoClient {

    private final RestClient restClient;

    public CatalogoClient(@Qualifier("catalogoRestClient") RestClient restClient) {
        this.restClient = restClient;
    }

    public DisponibilidadeDto consultarDisponibilidade(Long tituloId, TipoTitulo tipo) {
        String caminho = tipo == TipoTitulo.FILME ? "/catalogo/filmes/{id}/streamings" : "/catalogo/series/{id}/streamings";
        return restClient.get()
                .uri(caminho, tituloId)
                .retrieve()
                .body(DisponibilidadeDto.class);
    }
}
