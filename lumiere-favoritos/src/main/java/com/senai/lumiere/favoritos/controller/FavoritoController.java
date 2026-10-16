package com.senai.lumiere.favoritos.controller;

import com.senai.lumiere.favoritos.dto.FavoritoRequest;
import com.senai.lumiere.favoritos.dto.FavoritoResponse;
import com.senai.lumiere.favoritos.security.UsuarioAutenticado;
import com.senai.lumiere.favoritos.service.FavoritoService;
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
@RequestMapping("/favoritos")
@RequiredArgsConstructor
public class FavoritoController {

    private final FavoritoService favoritoService;

    @PostMapping
    public ResponseEntity<FavoritoResponse> adicionar(
            @AuthenticationPrincipal UsuarioAutenticado usuario,
            @Valid @RequestBody FavoritoRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED).body(favoritoService.adicionar(usuario.id(), request));
    }

    @GetMapping
    public List<FavoritoResponse> listar(@AuthenticationPrincipal UsuarioAutenticado usuario) {
        return favoritoService.listar(usuario.id());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> remover(@AuthenticationPrincipal UsuarioAutenticado usuario, @PathVariable Long id) {
        favoritoService.remover(usuario.id(), id);
        return ResponseEntity.noContent().build();
    }
}
