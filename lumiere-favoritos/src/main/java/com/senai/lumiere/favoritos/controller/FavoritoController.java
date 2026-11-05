package com.senai.lumiere.favoritos.controller;

import com.senai.lumiere.favoritos.dto.FavoritoRequest;
import com.senai.lumiere.favoritos.dto.FavoritoResponse;
import com.senai.lumiere.favoritos.exception.ErroResponse;
import com.senai.lumiere.favoritos.security.UsuarioAutenticado;
import com.senai.lumiere.favoritos.service.FavoritoService;
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

@Tag(name = "Favoritos", description = "Títulos marcados como favoritos pelo usuário logado")
@SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping("/favoritos")
@RequiredArgsConstructor
public class FavoritoController {

    private final FavoritoService favoritoService;

    @Operation(
            summary = "Favorita um título",
            description = "Antes de salvar, confere no `lumiere-catalogo` se o título existe mesmo. Se o catálogo estiver fora do ar, o favorito é aceito assim mesmo — uma falha de infraestrutura não deve virar 'título inválido' para o usuário."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Favorito criado"),
            @ApiResponse(responseCode = "400", description = "Corpo inválido (faltou `tituloId` ou `tipo`)",
                    content = @Content(schema = @Schema(implementation = ErroResponse.class))),
            @ApiResponse(responseCode = "401", description = "Token ausente, expirado ou inválido",
                    content = @Content(schema = @Schema(implementation = ErroResponse.class))),
            @ApiResponse(responseCode = "404", description = "O título não existe no catálogo",
                    content = @Content(schema = @Schema(implementation = ErroResponse.class))),
            @ApiResponse(responseCode = "409", description = "Esse título já está nos favoritos do usuário",
                    content = @Content(schema = @Schema(implementation = ErroResponse.class)))
    })
    @PostMapping
    public ResponseEntity<FavoritoResponse> adicionar(
            @Parameter(hidden = true) @AuthenticationPrincipal UsuarioAutenticado usuario,
            @Valid @RequestBody FavoritoRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED).body(favoritoService.adicionar(usuario.id(), request));
    }

    @Operation(summary = "Lista os favoritos do usuário", description = "Devolve só os favoritos de quem está logado.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lista carregada (vazia se o usuário não favoritou nada)"),
            @ApiResponse(responseCode = "401", description = "Token ausente, expirado ou inválido",
                    content = @Content(schema = @Schema(implementation = ErroResponse.class)))
    })
    @GetMapping
    public List<FavoritoResponse> listar(@Parameter(hidden = true) @AuthenticationPrincipal UsuarioAutenticado usuario) {
        return favoritoService.listar(usuario.id());
    }

    @Operation(
            summary = "Remove um favorito",
            description = "Só remove se o favorito pertencer ao usuário logado — tentar apagar o favorito de outra pessoa devolve 404, não 403."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Favorito removido"),
            @ApiResponse(responseCode = "401", description = "Token ausente, expirado ou inválido",
                    content = @Content(schema = @Schema(implementation = ErroResponse.class))),
            @ApiResponse(responseCode = "404", description = "Favorito não existe ou é de outro usuário",
                    content = @Content(schema = @Schema(implementation = ErroResponse.class)))
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> remover(
            @Parameter(hidden = true) @AuthenticationPrincipal UsuarioAutenticado usuario,
            @Parameter(description = "Id do favorito (não é o id do título)", example = "1") @PathVariable Long id
    ) {
        favoritoService.remover(usuario.id(), id);
        return ResponseEntity.noContent().build();
    }
}
