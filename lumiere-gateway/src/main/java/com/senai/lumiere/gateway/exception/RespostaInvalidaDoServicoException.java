package com.senai.lumiere.gateway.exception;

/**
 * O serviço respondeu, mas o gateway não conseguiu ler ou repassar a resposta (por exemplo, a
 * conexão caiu no meio do corpo). É o caso clássico do 502 Bad Gateway.
 */
public class RespostaInvalidaDoServicoException extends RuntimeException {

    public RespostaInvalidaDoServicoException(String caminho, Throwable causa) {
        super("O serviço devolveu uma resposta que o gateway não conseguiu repassar: " + caminho, causa);
    }

}
