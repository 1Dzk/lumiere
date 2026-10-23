package com.senai.lumiere.gateway.exception;

public class ServicoIndisponivelException extends RuntimeException {

    public ServicoIndisponivelException(String caminho, Throwable causa) {
        super("Serviço indisponível no momento para: " + caminho, causa);
    }

}
