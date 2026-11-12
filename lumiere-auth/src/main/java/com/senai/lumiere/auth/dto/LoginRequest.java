package com.senai.lumiere.auth.dto;

import com.senai.lumiere.auth.validation.MaxBytes;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.Locale;

@Schema(description = "Credenciais de acesso")
public record LoginRequest(

        @Schema(description = "E-mail cadastrado (maiúsculas e espaços nas pontas são ignorados)", example = "ana@lumiere.dev", maxLength = 255)
        @NotBlank(message = "Email é obrigatório")
        @Email(message = "Email inválido")
        @Size(max = 255, message = "Email deve ter no máximo 255 caracteres")
        String email,

        @Schema(description = "Senha da conta", example = "senha12345", maxLength = 72)
        @NotBlank(message = "Senha é obrigatória")
        @MaxBytes(value = 72, message = "Senha deve ter no máximo 72 bytes (acentos e emojis ocupam mais de 1)")
        String senha
) {

    /** Mesma normalização do cadastro, senão quem registrou em minúsculas não consegue logar digitando diferente. */
    public LoginRequest {
        email = email == null ? null : email.trim().toLowerCase(Locale.ROOT);
    }
}
