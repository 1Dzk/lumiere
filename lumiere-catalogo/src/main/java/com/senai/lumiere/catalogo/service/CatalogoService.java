package com.senai.lumiere.catalogo.service;

import com.senai.lumiere.catalogo.dto.DisponibilidadeStreamingResponse;
import com.senai.lumiere.catalogo.dto.GeneroResponse;
import com.senai.lumiere.catalogo.dto.ProvedorDisponivelResponse;
import com.senai.lumiere.catalogo.dto.TipoTitulo;
import com.senai.lumiere.catalogo.dto.TituloDetalheResponse;
import com.senai.lumiere.catalogo.dto.TituloResumoResponse;
import com.senai.lumiere.catalogo.dto.mapper.CatalogoMapper;
import com.senai.lumiere.catalogo.tmdb.TmdbClient;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CatalogoService {

    private static final Set<String> TIPOS_SUPORTADOS = Set.of("movie", "tv");

    /** Quantas páginas da TMDB (20 itens cada) combinar pro "em alta" — mais itens em destaque na home. */
    private static final int PAGINAS_EM_ALTA = 3;

    private final TmdbClient tmdbClient;
    private final CatalogoMapper mapper;
    private final GeneroService generoService;

    private Map<Long, String> mapaGeneros() {
        return generoService.listar().stream()
                .collect(Collectors.toMap(GeneroResponse::id, GeneroResponse::nome, (a, b) -> a));
    }

    @Cacheable("busca")
    public List<TituloResumoResponse> buscar(String titulo) {
        Map<Long, String> generosPorId = mapaGeneros();
        return tmdbClient.buscar(titulo).results().stream()
                .filter(item -> TIPOS_SUPORTADOS.contains(item.mediaType()))
                .map(item -> mapper.toResumo(item, generosPorId))
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
        Map<Long, String> generosPorId = mapaGeneros();
        List<TituloResumoResponse> resultado = new ArrayList<>();
        Set<Long> idsVistos = new HashSet<>();

        for (int pagina = 1; pagina <= PAGINAS_EM_ALTA; pagina++) {
            tmdbClient.emAlta(pagina).results().stream()
                    .filter(item -> TIPOS_SUPORTADOS.contains(item.mediaType()))
                    .filter(item -> idsVistos.add(item.id()))
                    .map(item -> mapper.toResumo(item, generosPorId))
                    .forEach(resultado::add);
        }

        return resultado;
    }

    @Cacheable("descobrir")
    public List<TituloResumoResponse> descobrir(TipoTitulo tipo, Long genero, int pagina) {
        Map<Long, String> generosPorId = mapaGeneros();
        List<TituloResumoResponse> resultado = new ArrayList<>();

        if (tipo == null || tipo == TipoTitulo.FILME) {
            tmdbClient.descobrirFilmes(genero, pagina).results().stream()
                    .map(item -> mapper.toResumo(item, TipoTitulo.FILME, generosPorId))
                    .forEach(resultado::add);
        }
        if (tipo == null || tipo == TipoTitulo.SERIE) {
            tmdbClient.descobrirSeries(genero, pagina).results().stream()
                    .map(item -> mapper.toResumo(item, TipoTitulo.SERIE, generosPorId))
                    .forEach(resultado::add);
        }

        return resultado;
    }

    @Cacheable("provedoresDisponiveis")
    public List<ProvedorDisponivelResponse> provedoresDisponiveis(String regiao) {
        Map<String, ProvedorDisponivelResponse> provedoresPorNome = new LinkedHashMap<>();

        tmdbClient.provedoresFilme(regiao).results().forEach(
                p -> provedoresPorNome.put(p.providerName(), mapper.toProvedorDisponivel(p)));
        tmdbClient.provedoresSerie(regiao).results().forEach(
                p -> provedoresPorNome.putIfAbsent(p.providerName(), mapper.toProvedorDisponivel(p)));

        return provedoresPorNome.values().stream()
                .sorted(Comparator.comparing(ProvedorDisponivelResponse::nome))
                .toList();
    }
}
