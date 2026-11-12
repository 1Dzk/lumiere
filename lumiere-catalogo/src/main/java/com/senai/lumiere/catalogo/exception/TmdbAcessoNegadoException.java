package com.senai.lumiere.catalogo.exception;


public class TmdbAcessoNegadoException extends RuntimeException {

    public TmdbAcessoNegadoException(Throwable causa) {
        super("A TMDB recusou o acesso do serviço", causa);
    }

}
