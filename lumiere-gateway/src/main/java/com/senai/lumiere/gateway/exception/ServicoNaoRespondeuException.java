package com.senai.lumiere.gateway.exception;

/**
 * O serviço aceitou a conexão (ou ela ficou pendente) mas não respondeu dentro do limite de tempo
 * do gateway. Diferente de "fora do ar": aqui o serviço pode até estar de pé, só lento demais.
 */
public class ServicoNaoRespondeuException extends RuntimeException {

    public ServicoNaoRespondeuException(String caminho, Throwable causa) {
        super("O serviço demorou demais para responder: " + caminho, causa);
    }

}
