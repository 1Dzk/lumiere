package com.senai.lumiere.favoritos.controller;

import com.senai.lumiere.favoritos.dto.DesejoInternoResponse;
import com.senai.lumiere.favoritos.model.Desejo;
import com.senai.lumiere.favoritos.repository.DesejoRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "Interno (serviço a serviço)",
        description = "Endpoint de uso interno do `lumiere-notificacoes`. Não exige token e não é exposto pelo gateway — "
                + "só é alcançável de dentro da rede Docker. Não deve ser consumido pela aplicação web.")
@RestController
@RequestMapping("/interno")
@RequiredArgsConstructor
public class InternoController {

    private final DesejoRepository desejoRepository;

    @Operation(
            summary = "Lista os desejos de todos os usuários",
            description = "Diferente de `GET /desejos`, que devolve só os do usuário logado, aqui vêm os desejos de todo mundo, com o `usuarioId` de cada um. É a lista que o job de notificações varre a cada rodada."
    )
    @ApiResponses(@ApiResponse(responseCode = "200", description = "Lista completa de desejos"))
    @GetMapping("/desejos")
    public List<DesejoInternoResponse> todosOsDesejos() {
        return desejoRepository.findAll().stream()
                .map(this::toResponse)
                .toList();
    }

    private DesejoInternoResponse toResponse(Desejo desejo) {
        return new DesejoInternoResponse(desejo.getId(), desejo.getUsuarioId(), desejo.getTituloId(), desejo.getTipoTitulo());
    }
}
