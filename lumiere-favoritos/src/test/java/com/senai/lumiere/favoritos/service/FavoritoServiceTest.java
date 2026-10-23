package com.senai.lumiere.favoritos.service;

import com.senai.lumiere.favoritos.client.CatalogoClient;
import com.senai.lumiere.favoritos.dto.FavoritoRequest;
import com.senai.lumiere.favoritos.dto.FavoritoResponse;
import com.senai.lumiere.favoritos.exception.ItemDuplicadoException;
import com.senai.lumiere.favoritos.exception.RecursoNaoEncontradoException;
import com.senai.lumiere.favoritos.exception.TituloInvalidoException;
import com.senai.lumiere.favoritos.model.Favorito;
import com.senai.lumiere.favoritos.model.TipoTitulo;
import com.senai.lumiere.favoritos.repository.FavoritoRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FavoritoServiceTest {

    @Mock
    private FavoritoRepository repository;

    @Mock
    private CatalogoClient catalogoClient;

    @InjectMocks
    private FavoritoService favoritoService;

    @Test
    void adicionar_deveSalvarQuandoTituloExisteENaoEDuplicado() {
        FavoritoRequest request = new FavoritoRequest(969681L, TipoTitulo.FILME);
        when(catalogoClient.tituloExiste(969681L, TipoTitulo.FILME)).thenReturn(true);
        when(repository.existsByUsuarioIdAndTituloIdAndTipoTitulo(1L, 969681L, TipoTitulo.FILME)).thenReturn(false);
        when(repository.save(any(Favorito.class))).thenAnswer(invocation -> invocation.getArgument(0));

        FavoritoResponse response = favoritoService.adicionar(1L, request);

        assertThat(response.tituloId()).isEqualTo(969681L);
        assertThat(response.tipo()).isEqualTo(TipoTitulo.FILME);
    }

    @Test
    void adicionar_deveLancarExcecaoQuandoTituloNaoExisteNoCatalogo() {
        FavoritoRequest request = new FavoritoRequest(999999999L, TipoTitulo.FILME);
        when(catalogoClient.tituloExiste(999999999L, TipoTitulo.FILME)).thenReturn(false);

        assertThatThrownBy(() -> favoritoService.adicionar(1L, request))
                .isInstanceOf(TituloInvalidoException.class);

        verify(repository, never()).save(any());
    }

    @Test
    void adicionar_deveLancarExcecaoQuandoJaFavoritado() {
        FavoritoRequest request = new FavoritoRequest(969681L, TipoTitulo.FILME);
        when(catalogoClient.tituloExiste(969681L, TipoTitulo.FILME)).thenReturn(true);
        when(repository.existsByUsuarioIdAndTituloIdAndTipoTitulo(1L, 969681L, TipoTitulo.FILME)).thenReturn(true);

        assertThatThrownBy(() -> favoritoService.adicionar(1L, request))
                .isInstanceOf(ItemDuplicadoException.class);
    }

    @Test
    void remover_deveLancarExcecaoQuandoFavoritoNaoPertenceAoUsuario() {
        when(repository.findByIdAndUsuarioId(5L, 1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> favoritoService.remover(1L, 5L))
                .isInstanceOf(RecursoNaoEncontradoException.class);
    }
}
