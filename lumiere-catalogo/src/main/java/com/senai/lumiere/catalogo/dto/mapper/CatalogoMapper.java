package com.senai.lumiere.catalogo.dto.mapper;

import com.senai.lumiere.catalogo.dto.DisponibilidadePorRegiao;
import com.senai.lumiere.catalogo.dto.DisponibilidadeStreamingResponse;
import com.senai.lumiere.catalogo.dto.ProvedorDisponivelResponse;
import com.senai.lumiere.catalogo.dto.ProvedorStreaming;
import com.senai.lumiere.catalogo.dto.TipoAcesso;
import com.senai.lumiere.catalogo.dto.TipoTitulo;
import com.senai.lumiere.catalogo.dto.TituloDetalheResponse;
import com.senai.lumiere.catalogo.dto.TituloResumoResponse;
import com.senai.lumiere.catalogo.tmdb.dto.TmdbFilmeDetalhe;
import com.senai.lumiere.catalogo.tmdb.dto.TmdbGenero;
import com.senai.lumiere.catalogo.tmdb.dto.TmdbItemResumo;
import com.senai.lumiere.catalogo.tmdb.dto.TmdbProvedor;
import com.senai.lumiere.catalogo.tmdb.dto.TmdbProvidersResponse;
import com.senai.lumiere.catalogo.tmdb.dto.TmdbRegiaoProviders;
import com.senai.lumiere.catalogo.tmdb.dto.TmdbSerieDetalhe;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component
public class CatalogoMapper {

    private final String imageBaseUrl;

    public CatalogoMapper(@Value("${tmdb.image.base-url}") String imageBaseUrl) {
        this.imageBaseUrl = imageBaseUrl;
    }

    private static final int MAX_GENEROS_NO_CARD = 6;

    public TituloResumoResponse toResumo(TmdbItemResumo item, Map<Long, String> generosPorId) {
        TipoTitulo tipo = "tv".equals(item.mediaType()) ? TipoTitulo.SERIE : TipoTitulo.FILME;
        return toResumo(item, tipo, generosPorId);
    }

    public TituloResumoResponse toResumo(TmdbItemResumo item, TipoTitulo tipo, Map<Long, String> generosPorId) {
        String titulo = tipo == TipoTitulo.SERIE ? item.name() : item.title();
        String dataLancamento = tipo == TipoTitulo.SERIE ? item.firstAirDate() : item.releaseDate();

        return new TituloResumoResponse(
                item.id(),
                tipo,
                titulo,
                montarImagemUrl(item.posterPath()),
                dataLancamento,
                item.overview(),
                nomesGenerosPorId(item.genreIds(), generosPorId),
                item.voteAverage()
        );
    }

    public TituloDetalheResponse toDetalheFilme(TmdbFilmeDetalhe filme) {
        return new TituloDetalheResponse(
                filme.id(),
                TipoTitulo.FILME,
                filme.title(),
                filme.originalTitle(),
                filme.overview(),
                montarImagemUrl(filme.posterPath()),
                montarImagemUrl(filme.backdropPath()),
                filme.releaseDate(),
                nomesGeneros(filme.genres()),
                filme.voteAverage(),
                filme.runtime(),
                null
        );
    }

    public TituloDetalheResponse toDetalheSerie(TmdbSerieDetalhe serie) {
        return new TituloDetalheResponse(
                serie.id(),
                TipoTitulo.SERIE,
                serie.name(),
                serie.originalName(),
                serie.overview(),
                montarImagemUrl(serie.posterPath()),
                montarImagemUrl(serie.backdropPath()),
                serie.firstAirDate(),
                nomesGeneros(serie.genres()),
                serie.voteAverage(),
                null,
                serie.numberOfSeasons()
        );
    }

    public DisponibilidadeStreamingResponse toDisponibilidade(Long tituloId, String regiaoSolicitada, TmdbProvidersResponse providers) {
        Map<String, TmdbRegiaoProviders> resultados = providers.results() == null ? Map.of() : providers.results();

        TmdbRegiaoProviders regiaoPedida = resultados.get(regiaoSolicitada.toUpperCase());
        List<ProvedorStreaming> streamingsNaRegiao = streamingsDaRegiao(regiaoPedida);

        if (!streamingsNaRegiao.isEmpty()) {
            return new DisponibilidadeStreamingResponse(
                    tituloId, regiaoSolicitada, true, streamingsNaRegiao, List.of(), regiaoPedida.link());
        }

        List<DisponibilidadePorRegiao> outrasRegioes = resultados.entrySet().stream()
                .filter(entry -> !entry.getKey().equalsIgnoreCase(regiaoSolicitada))
                .map(entry -> new DisponibilidadePorRegiao(
                        entry.getKey(), streamingsDaRegiao(entry.getValue()), entry.getValue().link()))
                .filter(regiao -> !regiao.streamings().isEmpty())
                .toList();

        return new DisponibilidadeStreamingResponse(tituloId, regiaoSolicitada, false, List.of(), outrasRegioes, null);
    }

    private List<ProvedorStreaming> streamingsDaRegiao(TmdbRegiaoProviders regiao) {
        if (regiao == null) {
            return List.of();
        }

        List<ProvedorStreaming> streamings = new ArrayList<>();
        adicionarProvedores(streamings, regiao.flatrate(), TipoAcesso.ASSINATURA);
        adicionarProvedores(streamings, regiao.rent(), TipoAcesso.ALUGUEL);
        adicionarProvedores(streamings, regiao.buy(), TipoAcesso.COMPRA);
        return streamings;
    }

    private void adicionarProvedores(List<ProvedorStreaming> destino, List<TmdbProvedor> origem, TipoAcesso tipoAcesso) {
        if (origem == null) {
            return;
        }

        origem.forEach(provedor -> destino.add(new ProvedorStreaming(
                provedor.providerName(),
                montarImagemUrl(provedor.logoPath()),
                tipoAcesso
        )));
    }

    public ProvedorDisponivelResponse toProvedorDisponivel(TmdbProvedor provedor) {
        return new ProvedorDisponivelResponse(provedor.providerName(), montarImagemUrl(provedor.logoPath()));
    }

    private List<String> nomesGeneros(List<TmdbGenero> generos) {
        return generos == null ? List.of() : generos.stream().map(TmdbGenero::name).toList();
    }

    private List<String> nomesGenerosPorId(List<Long> generoIds, Map<Long, String> generosPorId) {
        if (generoIds == null || generosPorId == null) {
            return List.of();
        }
        return generoIds.stream()
                .map(generosPorId::get)
                .filter(nome -> nome != null)
                .limit(MAX_GENEROS_NO_CARD)
                .toList();
    }

    private String montarImagemUrl(String path) {
        return path == null ? null : imageBaseUrl + path;
    }
}
