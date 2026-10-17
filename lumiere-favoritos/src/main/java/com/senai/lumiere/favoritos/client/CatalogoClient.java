package com.senai.lumiere.favoritos.client;

import com.senai.lumiere.favoritos.model.TipoTitulo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

@Slf4j
@Component
@RequiredArgsConstructor
public class CatalogoClient {

    private final RestClient catalogoRestClient;

    public boolean tituloExiste(Long tituloId, TipoTitulo tipo) {
        String caminho = tipo == TipoTitulo.FILME ? "/catalogo/filmes/{id}" : "/catalogo/series/{id}";

        try {
            catalogoRestClient.get().uri(caminho, tituloId).retrieve().toBodilessEntity();
            return true;
        } catch (HttpClientErrorException.NotFound ex) {
            return false;
        } catch (RestClientException ex) {
            log.warn("lumiere-catalogo indisponível ao validar título {} ({}), deixando passar", tituloId, tipo, ex);
            return true;
        }
    }
}
