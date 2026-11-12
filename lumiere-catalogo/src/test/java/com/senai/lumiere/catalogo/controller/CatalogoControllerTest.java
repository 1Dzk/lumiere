package com.senai.lumiere.catalogo.controller;

import com.senai.lumiere.catalogo.config.CacheConfig;
import com.senai.lumiere.catalogo.exception.GlobalExceptionHandler;
import com.senai.lumiere.catalogo.exception.TituloNaoEncontradoException;
import com.senai.lumiere.catalogo.exception.TmdbAcessoNegadoException;
import com.senai.lumiere.catalogo.exception.TmdbIndisponivelException;
import com.senai.lumiere.catalogo.exception.TmdbLimiteExcedidoException;
import com.senai.lumiere.catalogo.service.CatalogoService;
import com.senai.lumiere.catalogo.service.GeneroService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Prova o contrato de erro: cada falha precisa sair com o status certo e no formato ErroResponse.
 * O handler é importado de propósito; sem ele estaríamos testando o formato padrão do Spring.
 */
// CacheConfig: o @EnableCaching da aplicação exige um CacheManager, que o @WebMvcTest não carrega sozinho.
@WebMvcTest(CatalogoController.class)
@Import({GlobalExceptionHandler.class, CacheConfig.class})
class CatalogoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CatalogoService catalogoService;

    @MockitoBean
    private GeneroService generoService;

    // ---- 400: entrada inválida (nunca chega ao service) ----

    @Test
    void busca_semTitulo_deveRetornar400NoFormatoDeErro() throws Exception {
        mockMvc.perform(get("/catalogo/busca"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.mensagem").value("Requisição inválida"))
                .andExpect(jsonPath("$.timestamp").exists());
        verifyNoInteractions(catalogoService);
    }

    @Test
    void busca_comTituloEmBranco_deveRetornar400ComCampoNoMapaDeErros() throws Exception {
        mockMvc.perform(get("/catalogo/busca").param("titulo", " "))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.mensagem").value("Parâmetros inválidos"))
                .andExpect(jsonPath("$.erros.titulo").exists());
        verifyNoInteractions(catalogoService);
    }

    @Test
    void filme_comIdNaoNumerico_deveRetornar400() throws Exception {
        mockMvc.perform(get("/catalogo/filmes/abc"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
        verifyNoInteractions(catalogoService);
    }

    @Test
    void filme_comIdZeroOuNegativo_deveRetornar400() throws Exception {
        mockMvc.perform(get("/catalogo/filmes/0"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.erros.id").exists());
        mockMvc.perform(get("/catalogo/filmes/-1"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.erros.id").exists());
        verifyNoInteractions(catalogoService);
    }

    @Test
    void descobrir_comTipoDesconhecido_deveRetornar400() throws Exception {
        mockMvc.perform(get("/catalogo/descobrir").param("tipo", "XYZ"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
        verifyNoInteractions(catalogoService);
    }

    @Test
    void descobrir_comPaginaForaDoIntervalo_deveRetornar400() throws Exception {
        mockMvc.perform(get("/catalogo/descobrir").param("pagina", "-5"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.erros.pagina").exists());
        mockMvc.perform(get("/catalogo/descobrir").param("pagina", "501"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.erros.pagina").exists());
        verifyNoInteractions(catalogoService);
    }

    @Test
    void descobrir_comGeneroNegativo_deveRetornar400() throws Exception {
        mockMvc.perform(get("/catalogo/descobrir").param("genero", "-1"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.erros.genero").exists());
        verifyNoInteractions(catalogoService);
    }

    @Test
    void provedores_comRegiaoInvalida_deveRetornar400() throws Exception {
        mockMvc.perform(get("/catalogo/provedores").param("regiao", "XXXX"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.erros.regiao").exists());
        verifyNoInteractions(catalogoService);
    }

    @Test
    void provedores_comRegiaoEmMinuscula_deveNormalizarParaMaiuscula() throws Exception {
        mockMvc.perform(get("/catalogo/provedores").param("regiao", "br"))
                .andExpect(status().isOk());
        verify(catalogoService).provedoresDisponiveis("BR");
    }

    // ---- Erros de roteamento do Spring MVC no nosso formato ----

    @Test
    void metodoNaoPermitido_deveRetornar405NoFormatoDeErro() throws Exception {
        mockMvc.perform(post("/catalogo/busca"))
                .andExpect(status().isMethodNotAllowed())
                .andExpect(jsonPath("$.status").value(405))
                .andExpect(jsonPath("$.mensagem").exists());
    }

    @Test
    void rotaInexistente_deveRetornar404NoFormatoDeErro() throws Exception {
        mockMvc.perform(get("/catalogo/inexistente"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.mensagem").exists());
    }

    // ---- Exceções vindas do service/client ----

    @Test
    void filme_quandoServiceLancaTituloNaoEncontrado_deveRetornar404() throws Exception {
        when(catalogoService.detalheFilme(anyLong())).thenThrow(new TituloNaoEncontradoException(999L));

        mockMvc.perform(get("/catalogo/filmes/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.mensagem").value("Título não encontrado: 999"));
    }

    @Test
    void filme_quandoTmdbIndisponivel_deveRetornar503() throws Exception {
        when(catalogoService.detalheFilme(anyLong())).thenThrow(new TmdbIndisponivelException(new RuntimeException("rede")));

        mockMvc.perform(get("/catalogo/filmes/1"))
                .andExpect(status().isServiceUnavailable())
                .andExpect(jsonPath("$.status").value(503));
    }

    @Test
    void filme_quandoTmdbLimiteExcedido_deveRetornar503() throws Exception {
        when(catalogoService.detalheFilme(anyLong())).thenThrow(new TmdbLimiteExcedidoException(new RuntimeException("429")));

        mockMvc.perform(get("/catalogo/filmes/1"))
                .andExpect(status().isServiceUnavailable())
                .andExpect(jsonPath("$.status").value(503));
    }

    @Test
    void filme_quandoTmdbRecusaToken_deveRetornar502SemVazarDetalhe() throws Exception {
        when(catalogoService.detalheFilme(anyLong())).thenThrow(new TmdbAcessoNegadoException(new RuntimeException("401")));

        mockMvc.perform(get("/catalogo/filmes/1"))
                .andExpect(status().isBadGateway())
                .andExpect(jsonPath("$.status").value(502))
                .andExpect(jsonPath("$.mensagem").value("Serviço de catálogo temporariamente indisponível"));
    }

    @Test
    void busca_quandoErroInesperado_deveRetornar500SemVazarDetalhe() throws Exception {
        when(catalogoService.buscar(anyString())).thenThrow(new IllegalStateException("segredo interno"));

        mockMvc.perform(get("/catalogo/busca").param("titulo", "duna"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.status").value(500))
                .andExpect(jsonPath("$.mensagem").value("Erro interno. Tente novamente."));
    }
}
