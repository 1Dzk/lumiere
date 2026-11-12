package com.senai.lumiere.gateway.exception;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.boot.webmvc.error.ErrorController;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;

/**
 * Destino do redirecionamento interno do Tomcat para erros que acontecem ANTES do Spring (requisição
 * mal formada, cabeçalho grande demais...). Sem isto, esses erros sairiam no formato padrão do Spring Boot
 * e não no {@link ErroResponse}. Substitui o {@code BasicErrorController}.
 */
@RestController
public class ErroController implements ErrorController {

    @RequestMapping("/error")
    public ResponseEntity<ErroResponse> erro(HttpServletRequest request) {
        Object status = request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE);
        if (!(status instanceof Integer codigo)) {
            // alguém pediu /error diretamente: não há erro nenhum para explicar, então é só uma rota que não existe
            throw new RotaNaoEncontradaException("/error");
        }
        return ResponseEntity.status(codigo)
                .body(new ErroResponse(Instant.now(), codigo, MensagensPadrao.para(codigo), null));
    }
}
