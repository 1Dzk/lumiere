package com.senai.lumiere.catalogo.controller;

import com.senai.lumiere.catalogo.dto.DisponibilidadeStreamingResponse;
import com.senai.lumiere.catalogo.dto.TituloDetalheResponse;
import com.senai.lumiere.catalogo.dto.TituloResumoResponse;
import com.senai.lumiere.catalogo.service.CatalogoService;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/catalogo")
@RequiredArgsConstructor
@Validated
public class CatalogoController {

    private final CatalogoService catalogoService;

    @GetMapping("/busca")
    public List<TituloResumoResponse> buscar(@RequestParam @NotBlank String titulo) {
        return catalogoService.buscar(titulo);
    }

    @GetMapping("/filmes/{id}")
    public TituloDetalheResponse detalheFilme(@PathVariable Long id) {
        return catalogoService.detalheFilme(id);
    }

    @GetMapping("/series/{id}")
    public TituloDetalheResponse detalheSerie(@PathVariable Long id) {
        return catalogoService.detalheSerie(id);
    }

    @GetMapping("/filmes/{id}/streamings")
    public DisponibilidadeStreamingResponse streamingsFilme(
            @PathVariable Long id,
            @RequestParam(defaultValue = "BR") String regiao
    ) {
        return catalogoService.streamingsFilme(id, regiao);
    }

    @GetMapping("/series/{id}/streamings")
    public DisponibilidadeStreamingResponse streamingsSerie(
            @PathVariable Long id,
            @RequestParam(defaultValue = "BR") String regiao
    ) {
        return catalogoService.streamingsSerie(id, regiao);
    }

    @GetMapping("/em-alta")
    public List<TituloResumoResponse> emAlta() {
        return catalogoService.emAlta();
    }
}
