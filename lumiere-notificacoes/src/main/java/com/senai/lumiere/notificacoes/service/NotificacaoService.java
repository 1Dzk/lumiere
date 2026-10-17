package com.senai.lumiere.notificacoes.service;

import com.senai.lumiere.notificacoes.client.CatalogoClient;
import com.senai.lumiere.notificacoes.client.FavoritosClient;
import com.senai.lumiere.notificacoes.dto.DesejoDto;
import com.senai.lumiere.notificacoes.dto.DisponibilidadeDto;
import com.senai.lumiere.notificacoes.dto.ProvedorDto;
import com.senai.lumiere.notificacoes.model.NotificacaoEnviada;
import com.senai.lumiere.notificacoes.repository.NotificacaoEnviadaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificacaoService {

    private final FavoritosClient favoritosClient;
    private final CatalogoClient catalogoClient;
    private final NotificacaoEnviadaRepository repository;

    @Scheduled(fixedDelayString = "${notificacoes.intervalo-ms}")
    public void verificarDesejos() {
        List<DesejoDto> desejos;

        try {
            desejos = favoritosClient.listarTodosDesejos();
        } catch (Exception ex) {
            log.warn("lumiere-favoritos indisponível, tenta de novo na próxima rodada", ex);
            return;
        }

        desejos.forEach(desejo -> {
            try {
                processar(desejo);
            } catch (Exception ex) {
                log.warn("Falha ao verificar título {} do usuário {}, tenta de novo na próxima rodada",
                        desejo.tituloId(), desejo.usuarioId(), ex);
            }
        });
    }

    private void processar(DesejoDto desejo) {
        if (repository.existsByUsuarioIdAndTituloIdAndTipoTitulo(desejo.usuarioId(), desejo.tituloId(), desejo.tipo())) {
            return;
        }

        DisponibilidadeDto disponibilidade = catalogoClient.consultarDisponibilidade(desejo.tituloId(), desejo.tipo());

        if (!disponibilidade.disponivelNaRegiaoSolicitada()) {
            return;
        }

        String streamings = disponibilidade.streamingsNaRegiao().stream()
                .map(ProvedorDto::nome)
                .collect(Collectors.joining(", "));

        log.info("Usuário {} avisado: título {} ({}) disponível em: {}",
                desejo.usuarioId(), desejo.tituloId(), desejo.tipo(), streamings);

        NotificacaoEnviada notificacao = new NotificacaoEnviada();
        notificacao.setUsuarioId(desejo.usuarioId());
        notificacao.setTituloId(desejo.tituloId());
        notificacao.setTipoTitulo(desejo.tipo());
        repository.save(notificacao);
    }
}
