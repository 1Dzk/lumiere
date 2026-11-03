package com.senai.lumiere.favoritos.controller;

import com.senai.lumiere.favoritos.dto.StreamingSeguidoRequest;
import com.senai.lumiere.favoritos.dto.StreamingSeguidoResponse;
import com.senai.lumiere.favoritos.exception.ErroResponse;
import com.senai.lumiere.favoritos.security.UsuarioAutenticado;
import com.senai.lumiere.favoritos.service.StreamingSeguidoService;
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

@Tag(name = "Streamings seguidos", description = "Streamings que o usuário acompanha")
@SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping("/streamings-seguidos")
@RequiredArgsConstructor
public class StreamingSeguidoController {

    private final StreamingSeguidoService streamingSeguidoService;

    @Operation(
            summary = "Passa a seguir um streaming",
            description = "O nome é livre (ex.: Netflix, Max, Disney+) e a comparação ignora maiúsculas/minúsculas, então 'netflix' e 'Netflix' contam como o mesmo."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Streaming seguido"),
            @ApiResponse(responseCode = "400", description = "Nome do streaming vazio",
                    content = @Content(schema = @Schema(implementation = ErroResponse.class))),
            @ApiResponse(responseCode = "403", description = "Token ausente, expirado ou inválido", content = @Content),
            @ApiResponse(responseCode = "409", description = "O usuário já segue esse streaming",
                    content = @Content(schema = @Schema(implementation = ErroResponse.class)))
    })
    @PostMapping
    public ResponseEntity<StreamingSeguidoResponse> adicionar(
            @Parameter(hidden = true) @AuthenticationPrincipal UsuarioAutenticado usuario,
            @Valid @RequestBody StreamingSeguidoRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED).body(streamingSeguidoService.adicionar(usuario.id(), request));
    }

    @Operation(summary = "Lista os streamings seguidos", description = "Devolve só os streamings seguidos por quem está logado.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lista carregada"),
            @ApiResponse(responseCode = "403", description = "Token ausente, expirado ou inválido", content = @Content)
    })
    @GetMapping
    public List<StreamingSeguidoResponse> listar(@Parameter(hidden = true) @AuthenticationPrincipal UsuarioAutenticado usuario) {
        return streamingSeguidoService.listar(usuario.id());
    }

    @Operation(summary = "Deixa de seguir um streaming", description = "Só remove se o registro pertencer ao usuário logado.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Streaming removido"),
            @ApiResponse(responseCode = "403", description = "Token ausente, expirado ou inválido", content = @Content),
            @ApiResponse(responseCode = "404", description = "Registro não existe ou é de outro usuário",
                    content = @Content(schema = @Schema(implementation = ErroResponse.class)))
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> remover(
            @Parameter(hidden = true) @AuthenticationPrincipal UsuarioAutenticado usuario,
            @Parameter(description = "Id do registro de streaming seguido", example = "1") @PathVariable Long id
    ) {
        streamingSeguidoService.remover(usuario.id(), id);
        return ResponseEntity.noContent().build();
    }
}
