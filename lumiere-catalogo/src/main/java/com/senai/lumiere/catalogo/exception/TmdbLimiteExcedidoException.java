package com.senai.lumiere.catalogo.exception;


public class TmdbLimiteExcedidoException extends RuntimeException {

    public TmdbLimiteExcedidoException(Throwable causa) {
        super("Limite de requisições da TMDB excedido, tente novamente em instantes", causa);
    }

}
