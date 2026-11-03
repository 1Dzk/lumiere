package com.senai.lumiere.gateway.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(RotaNaoEncontradaException.class)
    public ResponseEntity<ErroResponse> handleRotaNaoEncontrada(RotaNaoEncontradaException ex) {
        return construir(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    @ExceptionHandler(ServicoIndisponivelException.class)
    public ResponseEntity<ErroResponse> handleServicoIndisponivel(ServicoIndisponivelException ex) {
        return construir(HttpStatus.SERVICE_UNAVAILABLE, ex.getMessage());
    }

    private ResponseEntity<ErroResponse> construir(HttpStatus status, String mensagem) {
        return ResponseEntity.status(status).body(new ErroResponse(Instant.now(), status.value(), mensagem, null));
    }
}
