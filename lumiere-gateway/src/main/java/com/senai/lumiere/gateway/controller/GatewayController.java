package com.senai.lumiere.gateway.controller;

import com.senai.lumiere.gateway.routing.RotaResolver;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestClient;

import java.util.Collections;
import java.util.Set;

@RestController
@RequiredArgsConstructor
public class GatewayController {

    private static final Set<String> CABECALHOS_IGNORADOS = Set.of("host", "content-length");

    private final RestClient restClient;
    private final RotaResolver rotaResolver;

    @RequestMapping("/**")
    public ResponseEntity<byte[]> encaminhar(HttpServletRequest request, @RequestBody(required = false) byte[] corpo) {
        String caminho = request.getRequestURI();
        String baseUrl = rotaResolver.resolver(caminho);
        String queryString = request.getQueryString();
        String uri = baseUrl + caminho + (queryString != null ? "?" + queryString : "");

        RestClient.RequestBodySpec spec = restClient.method(HttpMethod.valueOf(request.getMethod())).uri(uri);

        Collections.list(request.getHeaderNames()).forEach(nome -> {
            if (!CABECALHOS_IGNORADOS.contains(nome.toLowerCase())) {
                spec.header(nome, Collections.list(request.getHeaders(nome)).toArray(new String[0]));
            }
        });

        if (corpo != null && corpo.length > 0) {
            spec.body(corpo);
        }

        return spec.retrieve()
                .onStatus(HttpStatusCode::isError, (req, res) -> { })
                .toEntity(byte[].class);
    }
}
