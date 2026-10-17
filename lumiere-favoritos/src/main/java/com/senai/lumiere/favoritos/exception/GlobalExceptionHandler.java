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
    public ResponseEntity<Map<String, Object>> handleValidacao(MethodArgumentNotValidException ex) {
        Map<String, String> erros = new LinkedHashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(erro ->
                erros.put(erro.getField(), erro.getDefaultMessage()));

        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(corpoErro(HttpStatus.BAD_REQUEST, "Dados inválidos", erros));
    }

    @ExceptionHandler(ItemDuplicadoException.class)
    public ResponseEntity<Map<String, Object>> handleItemDuplicado(ItemDuplicadoException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(corpoErro(HttpStatus.CONFLICT, ex.getMessage(), null));
    }

    @ExceptionHandler(RecursoNaoEncontradoException.class)
    public ResponseEntity<Map<String, Object>> handleRecursoNaoEncontrado(RecursoNaoEncontradoException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(corpoErro(HttpStatus.NOT_FOUND, ex.getMessage(), null));
    }

    @ExceptionHandler(TituloInvalidoException.class)
    public ResponseEntity<Map<String, Object>> handleTituloInvalido(TituloInvalidoException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(corpoErro(HttpStatus.NOT_FOUND, ex.getMessage(), null));
    }

    private Map<String, Object> corpoErro(HttpStatus status, String mensagem, Map<String, String> erros) {
        Map<String, Object> corpo = new LinkedHashMap<>();
        corpo.put("timestamp", Instant.now());
        corpo.put("status", status.value());
        corpo.put("mensagem", mensagem);
        if (erros != null) {
            corpo.put("erros", erros);
        }
        return corpo;
    }
}
