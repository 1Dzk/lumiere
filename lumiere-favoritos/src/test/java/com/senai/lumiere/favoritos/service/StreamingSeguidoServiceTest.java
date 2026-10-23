package com.senai.lumiere.favoritos.service;

import com.senai.lumiere.favoritos.dto.StreamingSeguidoRequest;
import com.senai.lumiere.favoritos.dto.StreamingSeguidoResponse;
import com.senai.lumiere.favoritos.exception.ItemDuplicadoException;
import com.senai.lumiere.favoritos.exception.RecursoNaoEncontradoException;
import com.senai.lumiere.favoritos.model.StreamingSeguido;
import com.senai.lumiere.favoritos.repository.StreamingSeguidoRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class StreamingSeguidoServiceTest {

    @Mock
    private StreamingSeguidoRepository repository;

    @InjectMocks
    private StreamingSeguidoService streamingSeguidoService;

    @Test
    void adicionar_deveSalvarQuandoAindaNaoSegue() {
        StreamingSeguidoRequest request = new StreamingSeguidoRequest("Netflix");
        when(repository.existsByUsuarioIdAndNomeStreamingIgnoreCase(1L, "Netflix")).thenReturn(false);
        when(repository.save(any(StreamingSeguido.class))).thenAnswer(invocation -> invocation.getArgument(0));

        StreamingSeguidoResponse response = streamingSeguidoService.adicionar(1L, request);

        assertThat(response.nomeStreaming()).isEqualTo("Netflix");
    }

    @Test
    void adicionar_deveLancarExcecaoQuandoJaSegue() {
        StreamingSeguidoRequest request = new StreamingSeguidoRequest("Netflix");
        when(repository.existsByUsuarioIdAndNomeStreamingIgnoreCase(1L, "Netflix")).thenReturn(true);

        assertThatThrownBy(() -> streamingSeguidoService.adicionar(1L, request))
                .isInstanceOf(ItemDuplicadoException.class);
    }

    @Test
    void remover_deveLancarExcecaoQuandoNaoEncontrado() {
        when(repository.findByIdAndUsuarioId(5L, 1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> streamingSeguidoService.remover(1L, 5L))
                .isInstanceOf(RecursoNaoEncontradoException.class);
    }
}
