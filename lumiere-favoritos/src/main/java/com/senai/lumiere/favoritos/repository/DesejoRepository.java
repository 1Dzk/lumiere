package com.senai.lumiere.favoritos.repository;

import com.senai.lumiere.favoritos.model.Desejo;
import com.senai.lumiere.favoritos.model.TipoTitulo;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface DesejoRepository extends JpaRepository<Desejo, Long> {

    List<Desejo> findByUsuarioId(Long usuarioId);

    boolean existsByUsuarioIdAndTituloIdAndTipoTitulo(Long usuarioId, Long tituloId, TipoTitulo tipoTitulo);

    Optional<Desejo> findByIdAndUsuarioId(Long id, Long usuarioId);

}
