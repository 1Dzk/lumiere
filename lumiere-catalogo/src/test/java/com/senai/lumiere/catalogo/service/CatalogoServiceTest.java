package com.senai.lumiere.catalogo.service;

import com.senai.lumiere.catalogo.dto.TituloResumoResponse;
import com.senai.lumiere.catalogo.dto.mapper.CatalogoMapper;
import com.senai.lumiere.catalogo.tmdb.TmdbClient;
import com.senai.lumiere.catalogo.tmdb.dto.TmdbItemResumo;
import com.senai.lumiere.catalogo.tmdb.dto.TmdbListaResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CatalogoServiceTest {

    @Mock
    private TmdbClient tmdbClient;

    @Spy
    private CatalogoMapper mapper = new CatalogoMapper("https://image.tmdb.org/t/p/w500");

    @InjectMocks
    private CatalogoService catalogoService;

    @Test
    void buscar_deveIgnorarResultadosQueNaoSaoFilmeOuSerie() {
        TmdbItemResumo filme = new TmdbItemResumo(1L, "movie", "Duna", null, "sinopse", null, "2021-09-15", null);
        TmdbItemResumo pessoa = new TmdbItemResumo(2L, "person", "Ator Qualquer", null, null, null, null, null);
        when(tmdbClient.buscar("duna")).thenReturn(new TmdbListaResponse(List.of(filme, pessoa)));

        List<TituloResumoResponse> resultado = catalogoService.buscar("duna");

        assertThat(resultado).hasSize(1);
        assertThat(resultado.get(0).titulo()).isEqualTo("Duna");
    }

    @Test
    void emAlta_deveIgnorarResultadosQueNaoSaoFilmeOuSerie() {
        TmdbItemResumo serie = new TmdbItemResumo(3L, "tv", null, "Dark", "sinopse", null, null, "2017-12-01");
        TmdbItemResumo pessoa = new TmdbItemResumo(4L, "person", "Ator Qualquer", null, null, null, null, null);
        when(tmdbClient.emAlta()).thenReturn(new TmdbListaResponse(List.of(serie, pessoa)));

        List<TituloResumoResponse> resultado = catalogoService.emAlta();

        assertThat(resultado).hasSize(1);
        assertThat(resultado.get(0).titulo()).isEqualTo("Dark");
    }
}
