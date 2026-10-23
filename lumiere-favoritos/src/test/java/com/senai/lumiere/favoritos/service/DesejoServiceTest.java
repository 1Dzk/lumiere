package com.senai.lumiere.favoritos.service;

import com.senai.lumiere.favoritos.client.CatalogoClient;
import com.senai.lumiere.favoritos.dto.DesejoRequest;
import com.senai.lumiere.favoritos.dto.DesejoResponse;
import com.senai.lumiere.favoritos.exception.ItemDuplicadoException;
import com.senai.lumiere.favoritos.exception.RecursoNaoEncontradoException;
import com.senai.lumiere.favoritos.exception.TituloInvalidoException;
import com.senai.lumiere.favoritos.model.Desejo;
import com.senai.lumiere.favoritos.model.TipoTitulo;
import com.senai.lumiere.favoritos.repository.DesejoRepository;
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
class DesejoServiceTest {

    @Mock
    private DesejoRepository repository;

    @Mock
    private CatalogoClient catalogoClient;

    @InjectMocks
    private DesejoService desejoService;

    @Test
    void adicionar_deveSalvarQuandoTituloExisteENaoEDuplicado() {
        DesejoRequest request = new DesejoRequest(969681L, TipoTitulo.FILME);
        when(catalogoClient.tituloExiste(969681L, TipoTitulo.FILME)).thenReturn(true);
        when(repository.existsByUsuarioIdAndTituloIdAndTipoTitulo(1L, 969681L, TipoTitulo.FILME)).thenReturn(false);
        when(repository.save(any(Desejo.class))).thenAnswer(invocation -> invocation.getArgument(0));

        DesejoResponse response = desejoService.adicionar(1L, request);

        assertThat(response.tituloId()).isEqualTo(969681L);
        assertThat(response.tipo()).isEqualTo(TipoTitulo.FILME);
    }

    @Test
    void adicionar_deveLancarExcecaoQuandoTituloNaoExisteNoCatalogo() {
        DesejoRequest request = new DesejoRequest(999999999L, TipoTitulo.FILME);
        when(catalogoClient.tituloExiste(999999999L, TipoTitulo.FILME)).thenReturn(false);

        assertThatThrownBy(() -> desejoService.adicionar(1L, request))
                .isInstanceOf(TituloInvalidoException.class);

        verify(repository, never()).save(any());
    }

    @Test
    void adicionar_deveLancarExcecaoQuandoJaNaLista() {
        DesejoRequest request = new DesejoRequest(969681L, TipoTitulo.FILME);
        when(catalogoClient.tituloExiste(969681L, TipoTitulo.FILME)).thenReturn(true);
        when(repository.existsByUsuarioIdAndTituloIdAndTipoTitulo(1L, 969681L, TipoTitulo.FILME)).thenReturn(true);

        assertThatThrownBy(() -> desejoService.adicionar(1L, request))
                .isInstanceOf(ItemDuplicadoException.class);
    }

    @Test
    void remover_deveLancarExcecaoQuandoDesejoNaoPertenceAoUsuario() {
        when(repository.findByIdAndUsuarioId(5L, 1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> desejoService.remover(1L, 5L))
                .isInstanceOf(RecursoNaoEncontradoException.class);
    }
}
