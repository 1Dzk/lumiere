package com.senai.lumiere.catalogo.exception;

public class TituloNaoEncontradoException extends RuntimeException {

    public TituloNaoEncontradoException(Long id) {
        super("Título não encontrado: " + id);
    }

}
