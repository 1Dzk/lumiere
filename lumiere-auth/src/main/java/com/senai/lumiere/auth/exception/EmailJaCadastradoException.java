package com.senai.lumiere.auth.exception;

public class EmailJaCadastradoException extends RuntimeException {

    public EmailJaCadastradoException(String email) {
        super("Email já cadastrado: " + email);
    }

}
