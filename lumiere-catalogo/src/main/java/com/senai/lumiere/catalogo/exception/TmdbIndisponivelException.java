package com.senai.lumiere.catalogo.exception;

public class TmdbIndisponivelException extends RuntimeException {

    public TmdbIndisponivelException(Throwable causa) {
        super("Não foi possível falar com a TMDB agora", causa);
    }

}
