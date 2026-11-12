package com.senai.lumiere.auth.exception;

/**
 * O token é válido (assinatura e prazo), mas o usuário dono dele não existe mais, por exemplo porque a
 * conta foi apagada depois do login. É uma situação esperada, não um bug: o cliente deve descartar o
 * token e fazer login de novo.
 */
public class SessaoInvalidaException extends RuntimeException {

    public SessaoInvalidaException() {
        super("Sessão inválida. Faça login novamente.");
    }

}
