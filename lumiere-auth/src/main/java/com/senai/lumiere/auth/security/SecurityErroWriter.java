package com.senai.lumiere.auth.security;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
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
        // UTF-8 explícito: sem isso o getWriter() usa ISO-8859-1 e o acento sai quebrado ("inv?lido")
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        if (status == HttpStatus.UNAUTHORIZED) {
            // exigido pela especificação HTTP em toda resposta 401: diz ao cliente qual esquema de autenticação usar
            response.setHeader(HttpHeaders.WWW_AUTHENTICATE, "Bearer");
        }
        response.getWriter().write(
                "{\"timestamp\":\"%s\",\"status\":%d,\"mensagem\":\"%s\"}"
                        .formatted(Instant.now(), status.value(), escaparJson(mensagem)));
    }

    private static String escaparJson(String texto) {
        StringBuilder saida = new StringBuilder(texto.length() + 8);
        for (char c : texto.toCharArray()) {
            switch (c) {
                case '"' -> saida.append("\\\"");
                case '\\' -> saida.append("\\\\");
                case '\n' -> saida.append("\\n");
                case '\r' -> saida.append("\\r");
                case '\t' -> saida.append("\\t");
                default -> {
                    if (c < 0x20) {
                        saida.append("\\u%04x".formatted((int) c));
                    } else {
                        saida.append(c);
                    }
                }
            }
        }
        return saida.toString();
    }
}
