package com.senai.lumiere.favoritos.controller;

import com.senai.lumiere.favoritos.dto.DesejoRequest;
import com.senai.lumiere.favoritos.dto.DesejoResponse;
import com.senai.lumiere.favoritos.exception.ErroResponse;
import com.senai.lumiere.favoritos.security.UsuarioAutenticado;
import com.senai.lumiere.favoritos.service.DesejoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "Desejos", description = "Títulos que o usuário quer ser avisado quando chegarem em algum streaming")
@SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping("/desejos")
@RequiredArgsConstructor
public class DesejoController {

    private final DesejoService desejoService;

    @Operation(
            summary = "Adiciona um título à lista de desejos",
            description = "O `lumiere-notificacoes` varre essa lista periodicamente e avisa quando o título ficar disponível em algum streaming. Igual aos favoritos, o título é conferido no catálogo antes de salvar."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Desejo criado"),
            @ApiResponse(responseCode = "400", description = "Corpo inválido (faltou `tituloId` ou `tipo`)",
                    content = @Content(schema = @Schema(implementation = ErroResponse.class))),
            @ApiResponse(responseCode = "403", description = "Token ausente, expirado ou inválido", content = @Content),
            @ApiResponse(responseCode = "404", description = "O título não existe no catálogo",
                    content = @Content(schema = @Schema(implementation = ErroResponse.class))),
            @ApiResponse(responseCode = "409", description = "Esse título já está na lista de desejos do usuário",
                    content = @Content(schema = @Schema(implementation = ErroResponse.class)))
    })
    @PostMapping
    public ResponseEntity<DesejoResponse> adicionar(
            @Parameter(hidden = true) @AuthenticationPrincipal UsuarioAutenticado usuario,
            @Valid @RequestBody DesejoRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED).body(desejoService.adicionar(usuario.id(), request));
    }

    @Operation(summary = "Lista os desejos do usuário", description = "Devolve só os desejos de quem está logado.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lista carregada"),
            @ApiResponse(responseCode = "403", description = "Token ausente, expirado ou inválido", content = @Content)
    })
    @GetMapping
    public List<DesejoResponse> listar(@Parameter(hidden = true) @AuthenticationPrincipal UsuarioAutenticado usuario) {
        return desejoService.listar(usuario.id());
    }

    @Operation(summary = "Remove um desejo", description = "Só remove se o desejo pertencer ao usuário logado.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Desejo removido"),
            @ApiResponse(responseCode = "403", description = "Token ausente, expirado ou inválido", content = @Content),
            @ApiResponse(responseCode = "404", description = "Desejo não existe ou é de outro usuário",
                    content = @Content(schema = @Schema(implementation = ErroResponse.class)))
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> remover(
            @Parameter(hidden = true) @AuthenticationPrincipal UsuarioAutenticado usuario,
            @Parameter(description = "Id do desejo (não é o id do título)", example = "1") @PathVariable Long id
    ) {
        desejoService.remover(usuario.id(), id);
        return ResponseEntity.noContent().build();
    }
}
