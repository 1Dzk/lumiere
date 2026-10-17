package com.senai.lumiere.notificacoes.repository;

import com.senai.lumiere.notificacoes.model.NotificacaoEnviada;
import com.senai.lumiere.notificacoes.model.TipoTitulo;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NotificacaoEnviadaRepository extends JpaRepository<NotificacaoEnviada, Long> {

    boolean existsByUsuarioIdAndTituloIdAndTipoTitulo(Long usuarioId, Long tituloId, TipoTitulo tipoTitulo);

}
