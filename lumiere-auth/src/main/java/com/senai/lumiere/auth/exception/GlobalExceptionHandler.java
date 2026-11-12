package com.senai.lumiere.auth.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Único lugar que traduz exceção em resposta HTTP. Estende {@link ResponseEntityExceptionHandler} para
 * que os erros do próprio Spring MVC (corpo ausente, JSON quebrado, método ou tipo de conteúdo errado,
 * rota inexistente) também saiam no formato {@link ErroResponse}.
 * <p>
 * Importante neste serviço: erro que escapa daqui vira {@code sendError} e passa de novo pelo Spring
 * Security (ver o {@code DispatcherType.ERROR} no {@code SecurityConfig}).
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    @ExceptionHandler(EmailJaCadastradoException.class)
    public ResponseEntity<ErroResponse> handleEmailDuplicado(EmailJaCadastradoException ex) {
        return construir(HttpStatus.CONFLICT, ex.getMessage(), null);
    }

    @ExceptionHandler(CredenciaisInvalidasException.class)
    public ResponseEntity<ErroResponse> handleCredenciaisInvalidas(CredenciaisInvalidasException ex) {
        return construir(HttpStatus.UNAUTHORIZED, ex.getMessage(), null);
    }

    @ExceptionHandler(SessaoInvalidaException.class)
    public ResponseEntity<ErroResponse> handleSessaoInvalida(SessaoInvalidaException ex) {
        return construir(HttpStatus.UNAUTHORIZED, ex.getMessage(), null);
    }

    // Sem estes dois, o handler genérico abaixo engoliria as exceções do Security e as transformaria em 500.
    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ErroResponse> handleAutenticacao(AuthenticationException ex) {
        return construir(HttpStatus.UNAUTHORIZED, "Token ausente, expirado ou inválido", null);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErroResponse> handleAcessoNegado(AccessDeniedException ex) {
        return construir(HttpStatus.FORBIDDEN, "Acesso negado", null);
    }

    // Rede de segurança: nada escapa do contrato, e o detalhe vai pro log, nunca pro cliente.
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErroResponse> handleInesperado(Exception ex) {
        log.error("Erro inesperado", ex);
        return construir(HttpStatus.INTERNAL_SERVER_ERROR, "Erro interno. Tente novamente.", null);
    }

    // O pai já mapeia esta exceção; sobrescrevemos o método dele em vez de declarar outro @ExceptionHandler.
    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(
            MethodArgumentNotValidException ex, HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        Map<String, String> erros = new LinkedHashMap<>();
        // merge, não put: um campo pode quebrar mais de uma regra (senha "" falha @NotBlank e @Size)
        ex.getBindingResult().getFieldErrors().forEach(erro ->
                erros.merge(erro.getField(), String.valueOf(erro.getDefaultMessage()), (a, b) -> a + "; " + b));

        return corpo(HttpStatus.BAD_REQUEST, "Dados inválidos", erros);
    }

    @Override
    protected ResponseEntity<Object> handleHttpMessageNotReadable(
            HttpMessageNotReadableException ex, HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        return corpo(HttpStatus.BAD_REQUEST, "Corpo da requisição ausente ou com JSON inválido", null);
    }

    // Todos os outros handlers herdados de ResponseEntityExceptionHandler passam por aqui.
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
            case 415 -> "Tipo de conteúdo não suportado (use application/json)";
            default -> status.is5xxServerError() ? "Erro interno. Tente novamente." : "Requisição não pôde ser atendida";
        };
    }

    private ResponseEntity<ErroResponse> construir(HttpStatus status, String mensagem, Map<String, String> erros) {
        return ResponseEntity.status(status).body(new ErroResponse(Instant.now(), status.value(), mensagem, erros));
    }

    private ResponseEntity<Object> corpo(HttpStatus status, String mensagem, Map<String, String> erros) {
        return ResponseEntity.status(status).body(new ErroResponse(Instant.now(), status.value(), mensagem, erros));
    }
}
