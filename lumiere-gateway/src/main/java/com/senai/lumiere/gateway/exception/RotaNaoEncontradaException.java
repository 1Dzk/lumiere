package com.senai.lumiere.gateway.exception;

public class RotaNaoEncontradaException extends RuntimeException {

    public RotaNaoEncontradaException(String caminho) {
        super("Nenhum serviço mapeado para: " + caminho);
    }

}
