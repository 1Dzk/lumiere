package com.senai.lumiere.favoritos.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErroResponse> handleValidacao(MethodArgumentNotValidException ex) {
        Map<String, String> erros = new LinkedHashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(erro ->
                erros.put(erro.getField(), erro.getDefaultMessage()));

        return construir(HttpStatus.BAD_REQUEST, "Dados inválidos", erros);
    }

    @ExceptionHandler(ItemDuplicadoException.class)
    public ResponseEntity<ErroResponse> handleItemDuplicado(ItemDuplicadoException ex) {
        return construir(HttpStatus.CONFLICT, ex.getMessage(), null);
    }

    @ExceptionHandler(RecursoNaoEncontradoException.class)
    public ResponseEntity<ErroResponse> handleRecursoNaoEncontrado(RecursoNaoEncontradoException ex) {
        return construir(HttpStatus.NOT_FOUND, ex.getMessage(), null);
    }

    @ExceptionHandler(TituloInvalidoException.class)
    public ResponseEntity<ErroResponse> handleTituloInvalido(TituloInvalidoException ex) {
        return construir(HttpStatus.NOT_FOUND, ex.getMessage(), null);
    }

    private ResponseEntity<ErroResponse> construir(HttpStatus status, String mensagem, Map<String, String> erros) {
        return ResponseEntity.status(status).body(new ErroResponse(Instant.now(), status.value(), mensagem, erros));
    }
}
