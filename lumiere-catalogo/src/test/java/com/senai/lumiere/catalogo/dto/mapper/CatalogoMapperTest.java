package com.senai.lumiere.catalogo.dto.mapper;

import com.senai.lumiere.catalogo.dto.DisponibilidadeStreamingResponse;
import com.senai.lumiere.catalogo.dto.TipoTitulo;
import com.senai.lumiere.catalogo.dto.TituloResumoResponse;
import com.senai.lumiere.catalogo.tmdb.dto.TmdbItemResumo;
import com.senai.lumiere.catalogo.tmdb.dto.TmdbProvedor;
import com.senai.lumiere.catalogo.tmdb.dto.TmdbProvidersResponse;
import com.senai.lumiere.catalogo.tmdb.dto.TmdbRegiaoProviders;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class CatalogoMapperTest {

    private final CatalogoMapper mapper = new CatalogoMapper("https://image.tmdb.org/t/p/w500");

    @Test
    void toResumo_deveMapearComoSerieQuandoMediaTypeForTv() {
        TmdbItemResumo item = new TmdbItemResumo(
                1L, "tv", null, "Dark", "sinopse", "/poster.jpg", null, "2017-12-01", 8.4, List.of(18L));

        TituloResumoResponse resumo = mapper.toResumo(item, Map.of(18L, "Drama"));

        assertThat(resumo.tipo()).isEqualTo(TipoTitulo.SERIE);
        assertThat(resumo.titulo()).isEqualTo("Dark");
        assertThat(resumo.dataLancamento()).isEqualTo("2017-12-01");
        assertThat(resumo.posterUrl()).isEqualTo("https://image.tmdb.org/t/p/w500/poster.jpg");
        assertThat(resumo.notaMedia()).isEqualTo(8.4);
        assertThat(resumo.generos()).containsExactly("Drama");
    }

    @Test
    void toResumo_deveMapearComoFilmeQuandoMediaTypeNaoForTv() {
        TmdbItemResumo item = new TmdbItemResumo(
                2L, "movie", "Duna", null, "sinopse", "/poster.jpg", "2021-09-15", null, 7.9, List.of(878L, 12L, 28L));

        TituloResumoResponse resumo = mapper.toResumo(item, Map.of(878L, "Ficção científica", 12L, "Aventura"));

        assertThat(resumo.tipo()).isEqualTo(TipoTitulo.FILME);
        assertThat(resumo.titulo()).isEqualTo("Duna");
        assertThat(resumo.dataLancamento()).isEqualTo("2021-09-15");
        assertThat(resumo.notaMedia()).isEqualTo(7.9);
        assertThat(resumo.generos()).containsExactly("Ficção científica", "Aventura");
    }

    @Test
    void toDisponibilidade_deveMarcarDisponivelQuandoRegiaoSolicitadaTemStreaming() {
        TmdbProvidersResponse providers = new TmdbProvidersResponse(1L, Map.of(
                "BR", new TmdbRegiaoProviders(
                        "https://www.themoviedb.org/movie/1-title/watch?locale=BR",
                        List.of(new TmdbProvedor("Netflix", "/logo.png")), null, null)
        ));

        DisponibilidadeStreamingResponse disponibilidade = mapper.toDisponibilidade(1L, "BR", providers);

        assertThat(disponibilidade.disponivelNaRegiaoSolicitada()).isTrue();
        assertThat(disponibilidade.streamingsNaRegiao()).hasSize(1);
        assertThat(disponibilidade.streamingsNaRegiao().get(0).nome()).isEqualTo("Netflix");
        assertThat(disponibilidade.outrasRegioes()).isEmpty();
        assertThat(disponibilidade.linkAssistir()).isEqualTo("https://www.themoviedb.org/movie/1-title/watch?locale=BR");
    }

    @Test
    void toDisponibilidade_deveListarOutrasRegioesQuandoNaoDisponivelNaSolicitada() {
        TmdbProvidersResponse providers = new TmdbProvidersResponse(1L, Map.of(
                "BR", new TmdbRegiaoProviders(null, null, null, null),
                "US", new TmdbRegiaoProviders(
                        "https://www.themoviedb.org/movie/1-title/watch?locale=US",
                        List.of(new TmdbProvedor("Netflix", "/logo.png")), null, null)
        ));

        DisponibilidadeStreamingResponse disponibilidade = mapper.toDisponibilidade(1L, "BR", providers);

        assertThat(disponibilidade.disponivelNaRegiaoSolicitada()).isFalse();
        assertThat(disponibilidade.streamingsNaRegiao()).isEmpty();
        assertThat(disponibilidade.linkAssistir()).isNull();
        assertThat(disponibilidade.outrasRegioes()).hasSize(1);
        assertThat(disponibilidade.outrasRegioes().get(0).regiao()).isEqualTo("US");
        assertThat(disponibilidade.outrasRegioes().get(0).link()).isEqualTo("https://www.themoviedb.org/movie/1-title/watch?locale=US");
    }
}
