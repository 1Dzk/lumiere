package com.senai.lumiere.gateway.exception;

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

/**
 * Único lugar que traduz exceção do gateway em resposta HTTP. Só vale para erros do PRÓPRIO gateway: o que
 * o serviço responde (inclusive 4xx e 5xx) passa direto, sem passar por aqui.
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    @ExceptionHandler(RotaNaoEncontradaException.class)
    public ResponseEntity<ErroResponse> handleRotaNaoEncontrada(RotaNaoEncontradaException ex) {
        return construir(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    @ExceptionHandler(RequisicaoInvalidaException.class)
    public ResponseEntity<ErroResponse> handleRequisicaoInvalida(RequisicaoInvalidaException ex) {
        return construir(HttpStatus.BAD_REQUEST, ex.getMessage());
    }

    @ExceptionHandler(ServicoIndisponivelException.class)
    public ResponseEntity<ErroResponse> handleServicoIndisponivel(ServicoIndisponivelException ex) {
        log.warn("Serviço fora do ar: {}", ex.getMessage(), ex.getCause());
        return construir(HttpStatus.SERVICE_UNAVAILABLE, ex.getMessage());
    }

    @ExceptionHandler(ServicoNaoRespondeuException.class)
    public ResponseEntity<ErroResponse> handleServicoNaoRespondeu(ServicoNaoRespondeuException ex) {
        log.warn("Serviço não respondeu a tempo: {}", ex.getMessage());
        return construir(HttpStatus.GATEWAY_TIMEOUT, ex.getMessage());
    }

    @ExceptionHandler(RespostaInvalidaDoServicoException.class)
    public ResponseEntity<ErroResponse> handleRespostaInvalida(RespostaInvalidaDoServicoException ex) {
        log.error("Resposta do serviço não pôde ser repassada: {}", ex.getMessage(), ex.getCause());
        return construir(HttpStatus.BAD_GATEWAY, ex.getMessage());
    }

    // Rede de segurança: nada escapa do contrato, e o detalhe vai pro log, nunca pro cliente.
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErroResponse> handleInesperado(Exception ex) {
        log.error("Erro inesperado no gateway", ex);
        return construir(HttpStatus.INTERNAL_SERVER_ERROR, "Erro interno. Tente novamente.");
    }

    // Todos os handlers herdados de ResponseEntityExceptionHandler passam por aqui.
    @Override
    protected ResponseEntity<Object> handleExceptionInternal(
            Exception ex, Object body, HttpHeaders headers, HttpStatusCode statusCode, WebRequest request) {
        if (statusCode.is5xxServerError()) {
            log.error("Erro interno no processamento da requisição", ex);
        }
        ErroResponse erro = new ErroResponse(Instant.now(), statusCode.value(), MensagensPadrao.para(statusCode.value()), null);
        return ResponseEntity.status(statusCode).headers(headers).body(erro);
    }

    private ResponseEntity<ErroResponse> construir(HttpStatus status, String mensagem) {
        return ResponseEntity.status(status).body(new ErroResponse(Instant.now(), status.value(), mensagem, null));
    }
}
