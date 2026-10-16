package com.senai.lumiere.favoritos.repository;

import com.senai.lumiere.favoritos.model.Favorito;
import com.senai.lumiere.favoritos.model.TipoTitulo;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface FavoritoRepository extends JpaRepository<Favorito, Long> {

    List<Favorito> findByUsuarioId(Long usuarioId);

    boolean existsByUsuarioIdAndTituloIdAndTipoTitulo(Long usuarioId, Long tituloId, TipoTitulo tipoTitulo);

    Optional<Favorito> findByIdAndUsuarioId(Long id, Long usuarioId);

}
