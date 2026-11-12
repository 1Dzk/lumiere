package com.senai.lumiere.auth.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.nio.charset.StandardCharsets;

public class MaxBytesValidator implements ConstraintValidator<MaxBytes, CharSequence> {

    private int maximo;

    @Override
    public void initialize(MaxBytes anotacao) {
        this.maximo = anotacao.value();
    }

    @Override
    public boolean isValid(CharSequence valor, ConstraintValidatorContext contexto) {
        // null é válido aqui: quem exige presença é o @NotBlank
        return valor == null || valor.toString().getBytes(StandardCharsets.UTF_8).length <= maximo;
    }
}
