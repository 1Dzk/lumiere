package com.senai.lumiere.favoritos.service;

import com.senai.lumiere.favoritos.dto.StreamingSeguidoRequest;
import com.senai.lumiere.favoritos.dto.StreamingSeguidoResponse;
import com.senai.lumiere.favoritos.exception.ItemDuplicadoException;
import com.senai.lumiere.favoritos.exception.RecursoNaoEncontradoException;
import com.senai.lumiere.favoritos.model.StreamingSeguido;
import com.senai.lumiere.favoritos.repository.StreamingSeguidoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class StreamingSeguidoService {

    private final StreamingSeguidoRepository repository;

    public StreamingSeguidoResponse adicionar(Long usuarioId, StreamingSeguidoRequest request) {
        if (repository.existsByUsuarioIdAndNomeStreamingIgnoreCase(usuarioId, request.nomeStreaming())) {
            throw new ItemDuplicadoException("Streaming já está sendo seguido");
        }

        StreamingSeguido streamingSeguido = new StreamingSeguido();
        streamingSeguido.setUsuarioId(usuarioId);
        streamingSeguido.setNomeStreaming(request.nomeStreaming());

        return toResponse(repository.save(streamingSeguido));
    }

    public List<StreamingSeguidoResponse> listar(Long usuarioId) {
        return repository.findByUsuarioId(usuarioId).stream()
                .map(this::toResponse)
                .toList();
    }

    public void remover(Long usuarioId, Long id) {
        StreamingSeguido streamingSeguido = repository.findByIdAndUsuarioId(id, usuarioId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Streaming seguido não encontrado: " + id));

        repository.delete(streamingSeguido);
    }

    private StreamingSeguidoResponse toResponse(StreamingSeguido streamingSeguido) {
        return new StreamingSeguidoResponse(streamingSeguido.getId(), streamingSeguido.getNomeStreaming(), streamingSeguido.getCriadoEm());
    }
}
