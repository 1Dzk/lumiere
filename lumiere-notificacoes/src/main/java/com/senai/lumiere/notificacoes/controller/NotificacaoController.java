package com.senai.lumiere.notificacoes.controller;

import com.senai.lumiere.notificacoes.dto.NotificacaoEnviadaResponse;
import com.senai.lumiere.notificacoes.model.NotificacaoEnviada;
import com.senai.lumiere.notificacoes.repository.NotificacaoEnviadaRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "Notificações", description = "Histórico dos avisos já disparados pelo job")
@RestController
@RequestMapping("/notificacoes")
@RequiredArgsConstructor
public class NotificacaoController {

    private final NotificacaoEnviadaRepository repository;

    @Operation(
            summary = "Lista os avisos já enviados",
            description = "Cada registro significa: o título que o usuário desejava ficou disponível em algum streaming e o aviso foi disparado. O par usuário+título só aparece uma vez — é o que impede o job de avisar a mesma coisa repetidamente."
    )
    @ApiResponses(@ApiResponse(responseCode = "200", description = "Histórico carregado (vazio se nada foi avisado ainda)"))
    @GetMapping("/enviadas")
    public List<NotificacaoEnviadaResponse> listarEnviadas() {
        return repository.findAll().stream()
                .map(this::toResponse)
                .toList();
    }

    private NotificacaoEnviadaResponse toResponse(NotificacaoEnviada notificacao) {
        return new NotificacaoEnviadaResponse(
                notificacao.getId(),
                notificacao.getUsuarioId(),
                notificacao.getTituloId(),
                notificacao.getTipoTitulo(),
                notificacao.getEnviadaEm()
        );
    }
}
