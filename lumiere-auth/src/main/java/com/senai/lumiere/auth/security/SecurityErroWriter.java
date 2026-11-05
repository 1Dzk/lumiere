package com.senai.lumiere.auth.security;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;

import java.io.IOException;
import java.time.Instant;

/**
 * Escreve o corpo de erro (mesmo formato do {@code ErroResponse} usado pelo resto da API) nas
 * respostas do Spring Security, que não passam pelo {@code GlobalExceptionHandler}.
 */
final class SecurityErroWriter {

    private SecurityErroWriter() {
    }

    static void escrever(HttpServletResponse response, HttpStatus status, String mensagem) throws IOException {
        response.setStatus(status.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.getWriter().write(
                "{\"timestamp\":\"%s\",\"status\":%d,\"mensagem\":\"%s\"}"
                        .formatted(Instant.now(), status.value(), mensagem.replace("\"", "\\\"")));
    }
}
