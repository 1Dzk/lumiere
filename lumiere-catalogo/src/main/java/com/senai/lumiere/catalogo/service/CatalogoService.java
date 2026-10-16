package com.senai.lumiere.catalogo.service;

import com.senai.lumiere.catalogo.dto.DisponibilidadeStreamingResponse;
import com.senai.lumiere.catalogo.dto.TituloDetalheResponse;
import com.senai.lumiere.catalogo.dto.TituloResumoResponse;
import com.senai.lumiere.catalogo.dto.mapper.CatalogoMapper;
import com.senai.lumiere.catalogo.tmdb.TmdbClient;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class CatalogoService {

    private static final Set<String> TIPOS_SUPORTADOS = Set.of("movie", "tv");

    private final TmdbClient tmdbClient;
    private final CatalogoMapper mapper;

    @Cacheable("busca")
    public List<TituloResumoResponse> buscar(String titulo) {
        return tmdbClient.buscar(titulo).results().stream()
                .filter(item -> TIPOS_SUPORTADOS.contains(item.mediaType()))
                .map(mapper::toResumo)
                .toList();
    }

    @Cacheable("detalheFilme")
    public TituloDetalheResponse detalheFilme(Long id) {
        return mapper.toDetalheFilme(tmdbClient.detalheFilme(id));
    }

    @Cacheable("detalheSerie")
    public TituloDetalheResponse detalheSerie(Long id) {
        return mapper.toDetalheSerie(tmdbClient.detalheSerie(id));
    }

    @Cacheable("streamingsFilme")
    public DisponibilidadeStreamingResponse streamingsFilme(Long id, String regiao) {
        return mapper.toDisponibilidade(id, regiao, tmdbClient.providersFilme(id));
    }

    @Cacheable("streamingsSerie")
    public DisponibilidadeStreamingResponse streamingsSerie(Long id, String regiao) {
        return mapper.toDisponibilidade(id, regiao, tmdbClient.providersSerie(id));
    }

    @Cacheable("emAlta")
    public List<TituloResumoResponse> emAlta() {
        return tmdbClient.emAlta().results().stream()
                .filter(item -> TIPOS_SUPORTADOS.contains(item.mediaType()))
                .map(mapper::toResumo)
                .toList();
    }
}
