package com.senai.lumiere.catalogo.exception;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Path;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;


@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    @ExceptionHandler(TituloNaoEncontradoException.class)
    public ResponseEntity<ErroResponse> handleTituloNaoEncontrado(TituloNaoEncontradoException ex) {
        return construir(HttpStatus.NOT_FOUND, ex.getMessage(), null);
    }

    @ExceptionHandler(TmdbIndisponivelException.class)
    public ResponseEntity<ErroResponse> handleTmdbIndisponivel(TmdbIndisponivelException ex) {
        log.warn("A TMDB não respondeu como esperado", ex);
        return construir(HttpStatus.SERVICE_UNAVAILABLE, ex.getMessage(), null);
    }

    @ExceptionHandler(TmdbLimiteExcedidoException.class)
    public ResponseEntity<ErroResponse> handleTmdbLimiteExcedido(TmdbLimiteExcedidoException ex) {
        log.warn("Limite de requisições da TMDB excedido", ex);
        return construir(HttpStatus.SERVICE_UNAVAILABLE, ex.getMessage(), null);
    }

    // 502, não 401: quem falhou na autenticação foi o nosso token junto à TMDB, não o usuário.
    @ExceptionHandler(TmdbAcessoNegadoException.class)
    public ResponseEntity<ErroResponse> handleTmdbAcessoNegado(TmdbAcessoNegadoException ex) {
        log.error("A TMDB recusou a credencial do serviço: verifique tmdb.api.token", ex);
        return construir(HttpStatus.BAD_GATEWAY, "Serviço de catálogo temporariamente indisponível", null);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErroResponse> handleValidacao(ConstraintViolationException ex) {
        Map<String, String> erros = new LinkedHashMap<>();
        for (ConstraintViolation<?> violacao : ex.getConstraintViolations()) {
            erros.putIfAbsent(nomeDoParametro(violacao.getPropertyPath()), violacao.getMessage());
        }
        return construir(HttpStatus.BAD_REQUEST, "Parâmetros inválidos", erros);
    }

    // Rede de segurança: nada escapa do contrato, e o detalhe vai pro log, nunca pro cliente.
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErroResponse> handleInesperado(Exception ex) {
        log.error("Erro inesperado", ex);
        return construir(HttpStatus.INTERNAL_SERVER_ERROR, "Erro interno. Tente novamente.", null);
    }

    // Todos os handlers herdados de ResponseEntityExceptionHandler passam por aqui.
    @Override
    protected ResponseEntity<Object> handleExceptionInternal(
            Exception ex, Object body, HttpHeaders headers, HttpStatusCode statusCode, WebRequest request) {
        if (statusCode.is5xxServerError()) {
            log.error("Erro interno no processamento da requisição", ex);
        }
        ErroResponse erro = new ErroResponse(Instant.now(), statusCode.value(), mensagemPadrao(statusCode), null);
        return ResponseEntity.status(statusCode).headers(headers).body(erro);
    }

    private static String mensagemPadrao(HttpStatusCode status) {
        return switch (status.value()) {
            case 400 -> "Requisição inválida";
            case 404 -> "Recurso não encontrado";
            case 405 -> "Método HTTP não permitido para este endereço";
            case 406 -> "Formato de resposta não suportado";
            case 415 -> "Tipo de conteúdo não suportado";
            default -> status.is5xxServerError() ? "Erro interno. Tente novamente." : "Requisição não pôde ser atendida";
        };
    }

    // O path vem como "metodo.parametro" (ex.: "buscar.titulo"); só o último nó interessa ao cliente.
    private static String nomeDoParametro(Path caminho) {
        String nome = "";
        for (Path.Node no : caminho) {
            nome = no.getName();
        }
        return nome;
    }

    private ResponseEntity<ErroResponse> construir(HttpStatus status, String mensagem, Map<String, String> erros) {
        return ResponseEntity.status(status).body(new ErroResponse(Instant.now(), status.value(), mensagem, erros));
    }
}
