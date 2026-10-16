package com.senai.lumiere.favoritos.controller;

import com.senai.lumiere.favoritos.dto.StreamingSeguidoRequest;
import com.senai.lumiere.favoritos.dto.StreamingSeguidoResponse;
import com.senai.lumiere.favoritos.security.UsuarioAutenticado;
import com.senai.lumiere.favoritos.service.StreamingSeguidoService;
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

@RestController
@RequestMapping("/streamings-seguidos")
@RequiredArgsConstructor
public class StreamingSeguidoController {

    private final StreamingSeguidoService streamingSeguidoService;

    @PostMapping
    public ResponseEntity<StreamingSeguidoResponse> adicionar(
            @AuthenticationPrincipal UsuarioAutenticado usuario,
            @Valid @RequestBody StreamingSeguidoRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED).body(streamingSeguidoService.adicionar(usuario.id(), request));
    }

    @GetMapping
    public List<StreamingSeguidoResponse> listar(@AuthenticationPrincipal UsuarioAutenticado usuario) {
        return streamingSeguidoService.listar(usuario.id());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> remover(@AuthenticationPrincipal UsuarioAutenticado usuario, @PathVariable Long id) {
        streamingSeguidoService.remover(usuario.id(), id);
        return ResponseEntity.noContent().build();
    }
}
