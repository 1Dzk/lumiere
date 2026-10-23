package com.senai.lumiere.notificacoes.service;

import com.senai.lumiere.notificacoes.client.CatalogoClient;
import com.senai.lumiere.notificacoes.client.FavoritosClient;
import com.senai.lumiere.notificacoes.dto.DesejoDto;
import com.senai.lumiere.notificacoes.dto.DisponibilidadeDto;
import com.senai.lumiere.notificacoes.dto.ProvedorDto;
import com.senai.lumiere.notificacoes.model.NotificacaoEnviada;
import com.senai.lumiere.notificacoes.model.TipoTitulo;
import com.senai.lumiere.notificacoes.repository.NotificacaoEnviadaRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NotificacaoServiceTest {

    @Mock
    private FavoritosClient favoritosClient;

    @Mock
    private CatalogoClient catalogoClient;

    @Mock
    private NotificacaoEnviadaRepository repository;

    @InjectMocks
    private NotificacaoService notificacaoService;

    @Test
    void verificarDesejos_deveGravarNotificacaoQuandoTituloFicaDisponivel() {
        DesejoDto desejo = new DesejoDto(1L, 10L, 969681L, TipoTitulo.FILME);
        when(favoritosClient.listarTodosDesejos()).thenReturn(List.of(desejo));
        when(repository.existsByUsuarioIdAndTituloIdAndTipoTitulo(10L, 969681L, TipoTitulo.FILME)).thenReturn(false);
        when(catalogoClient.consultarDisponibilidade(969681L, TipoTitulo.FILME))
                .thenReturn(new DisponibilidadeDto(969681L, true, List.of(new ProvedorDto("Netflix"))));

        notificacaoService.verificarDesejos();

        verify(repository).save(any(NotificacaoEnviada.class));
    }

    @Test
    void verificarDesejos_deveIgnorarQuandoJaNotificadoAntes() {
        DesejoDto desejo = new DesejoDto(1L, 10L, 969681L, TipoTitulo.FILME);
        when(favoritosClient.listarTodosDesejos()).thenReturn(List.of(desejo));
        when(repository.existsByUsuarioIdAndTituloIdAndTipoTitulo(10L, 969681L, TipoTitulo.FILME)).thenReturn(true);

        notificacaoService.verificarDesejos();

        verify(catalogoClient, never()).consultarDisponibilidade(any(), any());
        verify(repository, never()).save(any());
    }

    @Test
    void verificarDesejos_deveIgnorarQuandoAindaNaoDisponivel() {
        DesejoDto desejo = new DesejoDto(1L, 10L, 969681L, TipoTitulo.FILME);
        when(favoritosClient.listarTodosDesejos()).thenReturn(List.of(desejo));
        when(repository.existsByUsuarioIdAndTituloIdAndTipoTitulo(10L, 969681L, TipoTitulo.FILME)).thenReturn(false);
        when(catalogoClient.consultarDisponibilidade(969681L, TipoTitulo.FILME))
                .thenReturn(new DisponibilidadeDto(969681L, false, List.of()));

        notificacaoService.verificarDesejos();

        verify(repository, never()).save(any());
    }

    @Test
    void verificarDesejos_naoDeveLancarExcecaoQuandoFavoritosEstaIndisponivel() {
        when(favoritosClient.listarTodosDesejos()).thenThrow(new RuntimeException("conexão recusada"));

        assertThatCode(() -> notificacaoService.verificarDesejos()).doesNotThrowAnyException();

        verify(repository, never()).save(any());
    }

    @Test
    void verificarDesejos_naoDeveInterromperOsDemaisQuandoUmDesejoFalha() {
        DesejoDto desejoComFalha = new DesejoDto(1L, 10L, 1L, TipoTitulo.FILME);
        DesejoDto desejoOk = new DesejoDto(2L, 11L, 2L, TipoTitulo.SERIE);
        when(favoritosClient.listarTodosDesejos()).thenReturn(List.of(desejoComFalha, desejoOk));
        when(repository.existsByUsuarioIdAndTituloIdAndTipoTitulo(10L, 1L, TipoTitulo.FILME)).thenReturn(false);
        when(repository.existsByUsuarioIdAndTituloIdAndTipoTitulo(11L, 2L, TipoTitulo.SERIE)).thenReturn(false);
        when(catalogoClient.consultarDisponibilidade(1L, TipoTitulo.FILME)).thenThrow(new RuntimeException("timeout"));
        when(catalogoClient.consultarDisponibilidade(2L, TipoTitulo.SERIE))
                .thenReturn(new DisponibilidadeDto(2L, true, List.of(new ProvedorDto("HBO Max"))));

        assertThatCode(() -> notificacaoService.verificarDesejos()).doesNotThrowAnyException();

        verify(repository).save(any(NotificacaoEnviada.class));
    }
}
