package com.senai.lumiere.auth.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * O texto não pode passar de {@code value} bytes em UTF-8.
 * <p>
 * Existe porque {@code @Size} conta caracteres, e o BCrypt limita a senha em 72 <b>bytes</b>: uma senha
 * com 60 letras acentuadas passa no {@code @Size(max = 72)} (60 caracteres) mas tem 120 bytes e é recusada.
 */
@Documented
@Constraint(validatedBy = MaxBytesValidator.class)
@Target({ElementType.METHOD, ElementType.FIELD, ElementType.ANNOTATION_TYPE, ElementType.CONSTRUCTOR,
        ElementType.PARAMETER, ElementType.TYPE_USE})
@Retention(RetentionPolicy.RUNTIME)
public @interface MaxBytes {

    int value();

    String message() default "Deve ter no máximo {value} bytes";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
