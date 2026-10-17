package com.senai.lumiere.favoritos.service;

import com.senai.lumiere.favoritos.client.CatalogoClient;
import com.senai.lumiere.favoritos.dto.FavoritoRequest;
import com.senai.lumiere.favoritos.dto.FavoritoResponse;
import com.senai.lumiere.favoritos.exception.ItemDuplicadoException;
import com.senai.lumiere.favoritos.exception.RecursoNaoEncontradoException;
import com.senai.lumiere.favoritos.exception.TituloInvalidoException;
import com.senai.lumiere.favoritos.model.Favorito;
import com.senai.lumiere.favoritos.repository.FavoritoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class FavoritoService {

    private final FavoritoRepository repository;
    private final CatalogoClient catalogoClient;

    public FavoritoResponse adicionar(Long usuarioId, FavoritoRequest request) {
        if (!catalogoClient.tituloExiste(request.tituloId(), request.tipo())) {
            throw new TituloInvalidoException(request.tituloId());
        }

        if (repository.existsByUsuarioIdAndTituloIdAndTipoTitulo(usuarioId, request.tituloId(), request.tipo())) {
            throw new ItemDuplicadoException("Título já favoritado");
        }

        Favorito favorito = new Favorito();
        favorito.setUsuarioId(usuarioId);
        favorito.setTituloId(request.tituloId());
        favorito.setTipoTitulo(request.tipo());

        return toResponse(repository.save(favorito));
    }

    public List<FavoritoResponse> listar(Long usuarioId) {
        return repository.findByUsuarioId(usuarioId).stream()
                .map(this::toResponse)
                .toList();
    }

    public void remover(Long usuarioId, Long id) {
        Favorito favorito = repository.findByIdAndUsuarioId(id, usuarioId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Favorito não encontrado: " + id));

        repository.delete(favorito);
    }

    private FavoritoResponse toResponse(Favorito favorito) {
        return new FavoritoResponse(favorito.getId(), favorito.getTituloId(), favorito.getTipoTitulo(), favorito.getCriadoEm());
    }
}
