package com.senai.lumiere.favoritos.repository;

import com.senai.lumiere.favoritos.model.StreamingSeguido;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface StreamingSeguidoRepository extends JpaRepository<StreamingSeguido, Long> {

    List<StreamingSeguido> findByUsuarioId(Long usuarioId);

    boolean existsByUsuarioIdAndNomeStreamingIgnoreCase(Long usuarioId, String nomeStreaming);

    Optional<StreamingSeguido> findByIdAndUsuarioId(Long id, Long usuarioId);

}
