package com.senai.lumiere.notificacoes.client;

import com.senai.lumiere.notificacoes.dto.DesejoDto;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.List;

@Component
public class FavoritosClient {

    private final RestClient restClient;

    public FavoritosClient(@Qualifier("favoritosRestClient") RestClient restClient) {
        this.restClient = restClient;
    }

    public List<DesejoDto> listarTodosDesejos() {
        return restClient.get()
                .uri("/interno/desejos")
                .retrieve()
                .body(new ParameterizedTypeReference<List<DesejoDto>>() { });
    }
}
