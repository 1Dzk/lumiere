package com.senai.lumiere.favoritos.service;

import com.senai.lumiere.favoritos.dto.DesejoRequest;
import com.senai.lumiere.favoritos.dto.DesejoResponse;
import com.senai.lumiere.favoritos.exception.ItemDuplicadoException;
import com.senai.lumiere.favoritos.exception.RecursoNaoEncontradoException;
import com.senai.lumiere.favoritos.model.Desejo;
import com.senai.lumiere.favoritos.repository.DesejoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class DesejoService {

    private final DesejoRepository repository;

    public DesejoResponse adicionar(Long usuarioId, DesejoRequest request) {
        if (repository.existsByUsuarioIdAndTituloIdAndTipoTitulo(usuarioId, request.tituloId(), request.tipo())) {
            throw new ItemDuplicadoException("Título já está na lista de desejos");
        }

        Desejo desejo = new Desejo();
        desejo.setUsuarioId(usuarioId);
        desejo.setTituloId(request.tituloId());
        desejo.setTipoTitulo(request.tipo());

        return toResponse(repository.save(desejo));
    }

    public List<DesejoResponse> listar(Long usuarioId) {
        return repository.findByUsuarioId(usuarioId).stream()
                .map(this::toResponse)
                .toList();
    }

    public void remover(Long usuarioId, Long id) {
        Desejo desejo = repository.findByIdAndUsuarioId(id, usuarioId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Desejo não encontrado: " + id));

        repository.delete(desejo);
    }

    private DesejoResponse toResponse(Desejo desejo) {
        return new DesejoResponse(desejo.getId(), desejo.getTituloId(), desejo.getTipoTitulo(), desejo.getCriadoEm());
    }
}
