package com.senai.lumiere.favoritos.controller;

import com.senai.lumiere.favoritos.dto.DesejoInternoResponse;
import com.senai.lumiere.favoritos.model.Desejo;
import com.senai.lumiere.favoritos.repository.DesejoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/interno")
@RequiredArgsConstructor
public class InternoController {

    private final DesejoRepository desejoRepository;

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
