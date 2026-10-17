package com.senai.lumiere.notificacoes.controller;

import com.senai.lumiere.notificacoes.dto.NotificacaoEnviadaResponse;
import com.senai.lumiere.notificacoes.model.NotificacaoEnviada;
import com.senai.lumiere.notificacoes.repository.NotificacaoEnviadaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/notificacoes")
@RequiredArgsConstructor
public class NotificacaoController {

    private final NotificacaoEnviadaRepository repository;

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
