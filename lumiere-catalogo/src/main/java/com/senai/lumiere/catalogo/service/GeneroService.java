package com.senai.lumiere.catalogo.service;

import com.senai.lumiere.catalogo.dto.GeneroResponse;
import com.senai.lumiere.catalogo.tmdb.TmdbClient;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Serviço à parte (em vez de um método dentro de {@link CatalogoService}) porque o cache do Spring
 * só funciona em chamadas entre beans — um método público chamando outro método público do mesmo
 * bean não passa pelo proxy de cache.
 */
@Service
@RequiredArgsConstructor
public class GeneroService {

    private final TmdbClient tmdbClient;

    @Cacheable("generos")
    public List<GeneroResponse> listar() {
        Map<Long, String> generosPorId = new LinkedHashMap<>();
        tmdbClient.generosFilme().genres().forEach(g -> generosPorId.put(g.id(), g.name()));
        tmdbClient.generosSerie().genres().forEach(g -> generosPorId.putIfAbsent(g.id(), g.name()));

        return generosPorId.entrySet().stream()
                .map(entry -> new GeneroResponse(entry.getKey(), entry.getValue()))
                .sorted(Comparator.comparing(GeneroResponse::nome))
                .toList();
    }
}
