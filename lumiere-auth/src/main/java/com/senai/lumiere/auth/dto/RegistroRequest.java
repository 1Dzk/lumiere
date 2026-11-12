package com.senai.lumiere.auth.dto;

import com.senai.lumiere.auth.validation.MaxBytes;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.Locale;

@Schema(description = "Dados para criar uma conta")
public record RegistroRequest(

        @Schema(description = "Nome do usuário", example = "Ana Souza", maxLength = 255)
        @NotBlank(message = "Nome é obrigatório")
        @Size(max = 255, message = "Nome deve ter no máximo 255 caracteres")
        String nome,

        @Schema(description = "E-mail, precisa ser único (guardado em minúsculas, sem espaços nas pontas)", example = "ana@lumiere.dev", maxLength = 255)
        @NotBlank(message = "Email é obrigatório")
        @Email(message = "Email inválido")
        @Size(max = 255, message = "Email deve ter no máximo 255 caracteres")
        String email,

        @Schema(description = "Senha com no mínimo 8 caracteres e no máximo 72 bytes (limite do BCrypt; acentos e emojis ocupam mais de 1 byte)", example = "senha12345", minLength = 8, maxLength = 72)
        @NotBlank(message = "Senha é obrigatória")
        @Size(min = 8, message = "Senha deve ter no mínimo 8 caracteres")
        @MaxBytes(value = 72, message = "Senha deve ter no máximo 72 bytes (acentos e emojis ocupam mais de 1)")
        String senha
) {

    /** Normaliza na entrada: "Ana@X.com " e "ana@x.com" são a mesma conta. */
    public RegistroRequest {
        email = email == null ? null : email.trim().toLowerCase(Locale.ROOT);
    }
}
