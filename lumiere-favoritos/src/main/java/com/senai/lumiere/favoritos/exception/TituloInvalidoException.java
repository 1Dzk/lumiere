package com.senai.lumiere.favoritos.exception;

public class TituloInvalidoException extends RuntimeException {

    public TituloInvalidoException(Long tituloId) {
        super("Título não encontrado no catálogo: " + tituloId);
    }

}
