package com.senai.lumiere.gateway.exception;

/**
 * A requisição que chegou ao gateway não pode ser repassada como está (por exemplo, uma URL com
 * codificação de porcentagem quebrada). É erro de quem chamou: 400.
 */
public class RequisicaoInvalidaException extends RuntimeException {

    public RequisicaoInvalidaException(String mensagem) {
        super(mensagem);
    }

}
