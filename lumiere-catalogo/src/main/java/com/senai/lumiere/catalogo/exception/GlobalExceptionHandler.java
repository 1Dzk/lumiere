package com.senai.lumiere.catalogo.exception;

import jakarta.validation.ConstraintViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(TituloNaoEncontradoException.class)
    public ResponseEntity<ErroResponse> handleTituloNaoEncontrado(TituloNaoEncontradoException ex) {
        return construir(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    @ExceptionHandler(TmdbIndisponivelException.class)
    public ResponseEntity<ErroResponse> handleTmdbIndisponivel(TmdbIndisponivelException ex) {
        return construir(HttpStatus.SERVICE_UNAVAILABLE, ex.getMessage());
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErroResponse> handleValidacao(ConstraintViolationException ex) {
        return construir(HttpStatus.BAD_REQUEST, ex.getMessage());
    }

    private ResponseEntity<ErroResponse> construir(HttpStatus status, String mensagem) {
        return ResponseEntity.status(status).body(new ErroResponse(Instant.now(), status.value(), mensagem, null));
    }
}
