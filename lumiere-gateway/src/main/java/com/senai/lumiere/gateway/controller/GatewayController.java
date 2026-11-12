package com.senai.lumiere.gateway.controller;

import com.senai.lumiere.gateway.exception.RequisicaoInvalidaException;
import com.senai.lumiere.gateway.routing.Encaminhador;
import com.senai.lumiere.gateway.routing.RotaResolver;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.util.Collections;

@RestController
@RequiredArgsConstructor
public class GatewayController {

    private final RotaResolver rotaResolver;
    private final Encaminhador encaminhador;

    @RequestMapping("/**")
    public ResponseEntity<byte[]> encaminhar(HttpServletRequest request, @RequestBody(required = false) byte[] corpo) {
        // caminho e query CRUS (ainda codificados), exatamente como o cliente mandou
        String caminho = request.getRequestURI();
        String baseUrl = rotaResolver.resolver(caminho);
        URI destino = montarDestino(baseUrl, caminho, request.getQueryString());

        return encaminhador.encaminhar(
                HttpMethod.valueOf(request.getMethod()), destino, cabecalhosDe(request), corpo, caminho);
    }

    /**
     * Monta a URI sem recodificar: "am%C3%A9lie%20poulain" chega ao serviço exatamente assim. Texto com
     * porcentagem quebrada (por exemplo "%ZZ") não vira URI válida, e isso é erro de quem chamou.
     */
    private static URI montarDestino(String baseUrl, String caminho, String queryString) {
        try {
            return URI.create(baseUrl + caminho + (queryString != null ? "?" + queryString : ""));
        } catch (IllegalArgumentException ex) {
            throw new RequisicaoInvalidaException("URL inválida: confira a codificação dos caracteres (por exemplo, %XX)");
        }
    }

    private static HttpHeaders cabecalhosDe(HttpServletRequest request) {
        HttpHeaders cabecalhos = new HttpHeaders();
        Collections.list(request.getHeaderNames())
                .forEach(nome -> cabecalhos.addAll(nome, Collections.list(request.getHeaders(nome))));
        return cabecalhos;
    }
}
