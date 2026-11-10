package com.senai.lumiere.catalogo.controller;

import com.senai.lumiere.catalogo.dto.DisponibilidadeStreamingResponse;
import com.senai.lumiere.catalogo.dto.GeneroResponse;
import com.senai.lumiere.catalogo.dto.ProvedorDisponivelResponse;
import com.senai.lumiere.catalogo.dto.TipoTitulo;
import com.senai.lumiere.catalogo.dto.TituloDetalheResponse;
import com.senai.lumiere.catalogo.dto.TituloResumoResponse;
import com.senai.lumiere.catalogo.exception.ErroResponse;
import com.senai.lumiere.catalogo.service.CatalogoService;
import com.senai.lumiere.catalogo.service.GeneroService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "Catálogo", description = "Busca de títulos e disponibilidade em streamings — acesso público, sem login")
@RestController
@RequestMapping("/catalogo")
@RequiredArgsConstructor
@Validated
public class CatalogoController {

    private final CatalogoService catalogoService;
    private final GeneroService generoService;

    @Operation(
            summary = "Busca filmes e séries pelo nome",
            description = "Pesquisa na TMDB e devolve só filmes e séries (pessoas são descartadas do resultado)."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Resultados da busca (lista vazia se nada bateu)"),
            @ApiResponse(responseCode = "400", description = "Parâmetro `titulo` vazio",
                    content = @Content(schema = @Schema(implementation = ErroResponse.class))),
            @ApiResponse(responseCode = "503", description = "A TMDB não respondeu",
                    content = @Content(schema = @Schema(implementation = ErroResponse.class)))
    })
    @GetMapping("/busca")
    public List<TituloResumoResponse> buscar(
            @Parameter(description = "Texto a procurar no nome do título", example = "duna", required = true)
            @RequestParam @NotBlank String titulo
    ) {
        return catalogoService.buscar(titulo);
    }

    @Operation(summary = "Detalhes de um filme", description = "Sinopse, gêneros, nota, duração e imagens de um filme.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Filme encontrado"),
            @ApiResponse(responseCode = "404", description = "Não existe filme com esse id na TMDB",
                    content = @Content(schema = @Schema(implementation = ErroResponse.class))),
            @ApiResponse(responseCode = "503", description = "A TMDB não respondeu",
                    content = @Content(schema = @Schema(implementation = ErroResponse.class)))
    })
    @GetMapping("/filmes/{id}")
    public TituloDetalheResponse detalheFilme(
            @Parameter(description = "Id do filme na TMDB", example = "969681") @PathVariable Long id
    ) {
        return catalogoService.detalheFilme(id);
    }

    @Operation(summary = "Detalhes de uma série", description = "Sinopse, gêneros, nota, número de temporadas e imagens de uma série.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Série encontrada"),
            @ApiResponse(responseCode = "404", description = "Não existe série com esse id na TMDB",
                    content = @Content(schema = @Schema(implementation = ErroResponse.class))),
            @ApiResponse(responseCode = "503", description = "A TMDB não respondeu",
                    content = @Content(schema = @Schema(implementation = ErroResponse.class)))
    })
    @GetMapping("/series/{id}")
    public TituloDetalheResponse detalheSerie(
            @Parameter(description = "Id da série na TMDB", example = "123542") @PathVariable Long id
    ) {
        return catalogoService.detalheSerie(id);
    }

    @Operation(
            summary = "Em quais streamings o filme está",
            description = """
                    Procura o filme nos streamings da região pedida. Se não achar nada na região, \
                    devolve `disponivelNaRegiaoSolicitada = false` e preenche `outrasRegioes` com os \
                    países onde o título está disponível — é assim que o Lumiere responde "ainda não \
                    chegou no Brasil, mas está em tal lugar".
                    """
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Disponibilidade consultada"),
            @ApiResponse(responseCode = "404", description = "Não existe filme com esse id na TMDB",
                    content = @Content(schema = @Schema(implementation = ErroResponse.class))),
            @ApiResponse(responseCode = "503", description = "A TMDB não respondeu",
                    content = @Content(schema = @Schema(implementation = ErroResponse.class)))
    })
    @GetMapping("/filmes/{id}/streamings")
    public DisponibilidadeStreamingResponse streamingsFilme(
            @Parameter(description = "Id do filme na TMDB", example = "969681") @PathVariable Long id,
            @Parameter(description = "País no formato ISO 3166-1 (BR, US, PT...)", example = "BR")
            @RequestParam(defaultValue = "BR") String regiao
    ) {
        return catalogoService.streamingsFilme(id, regiao);
    }

    @Operation(
            summary = "Em quais streamings a série está",
            description = "Mesma regra do endpoint de filmes: se não houver streaming na região pedida, devolve onde o título está em outros países."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Disponibilidade consultada"),
            @ApiResponse(responseCode = "404", description = "Não existe série com esse id na TMDB",
                    content = @Content(schema = @Schema(implementation = ErroResponse.class))),
            @ApiResponse(responseCode = "503", description = "A TMDB não respondeu",
                    content = @Content(schema = @Schema(implementation = ErroResponse.class)))
    })
    @GetMapping("/series/{id}/streamings")
    public DisponibilidadeStreamingResponse streamingsSerie(
            @Parameter(description = "Id da série na TMDB", example = "123542") @PathVariable Long id,
            @Parameter(description = "País no formato ISO 3166-1 (BR, US, PT...)", example = "BR")
            @RequestParam(defaultValue = "BR") String regiao
    ) {
        return catalogoService.streamingsSerie(id, regiao);
    }

    @Operation(summary = "Filmes e séries em alta", description = "Lista do que está em alta hoje na TMDB — é o que a tela inicial do app mostra.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lista carregada"),
            @ApiResponse(responseCode = "503", description = "A TMDB não respondeu",
                    content = @Content(schema = @Schema(implementation = ErroResponse.class)))
    })
    @GetMapping("/em-alta")
    public List<TituloResumoResponse> emAlta() {
        return catalogoService.emAlta();
    }

    @Operation(summary = "Gêneros disponíveis para filtrar o catálogo", description = "Lista combinada de gêneros de filme e série da TMDB, pra montar um filtro.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lista carregada"),
            @ApiResponse(responseCode = "503", description = "A TMDB não respondeu",
                    content = @Content(schema = @Schema(implementation = ErroResponse.class)))
    })
    @GetMapping("/generos")
    public List<GeneroResponse> generos() {
        return generoService.listar();
    }

    @Operation(
            summary = "Descobre títulos por filtro",
            description = "Navega o catálogo além do em-alta: filtra por tipo (FILME/SERIE) e/ou gênero, ordenado por popularidade."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Resultados filtrados (lista vazia se nada bateu)"),
            @ApiResponse(responseCode = "503", description = "A TMDB não respondeu",
                    content = @Content(schema = @Schema(implementation = ErroResponse.class)))
    })
    @GetMapping("/descobrir")
    public List<TituloResumoResponse> descobrir(
            @Parameter(description = "Filtra só filme ou só série; omitido traz os dois") @RequestParam(required = false) TipoTitulo tipo,
            @Parameter(description = "Id de gênero da TMDB (ver /catalogo/generos)", example = "28") @RequestParam(required = false) Long genero,
            @Parameter(description = "Página de resultados (20 por página)", example = "1") @RequestParam(defaultValue = "1") int pagina
    ) {
        return catalogoService.descobrir(tipo, genero, pagina);
    }

    @Operation(
            summary = "Streamings que existem de verdade, pra escolher ao seguir um",
            description = "Lista todo provedor de filme/série que a TMDB conhece numa região — usar como opções ao seguir um streaming, em vez de texto livre."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lista carregada"),
            @ApiResponse(responseCode = "503", description = "A TMDB não respondeu",
                    content = @Content(schema = @Schema(implementation = ErroResponse.class)))
    })
    @GetMapping("/provedores")
    public List<ProvedorDisponivelResponse> provedores(
            @Parameter(description = "País no formato ISO 3166-1 (BR, US, PT...)", example = "BR")
            @RequestParam(defaultValue = "BR") String regiao
    ) {
        return catalogoService.provedoresDisponiveis(regiao);
    }
}
