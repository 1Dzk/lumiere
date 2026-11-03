package com.senai.lumiere.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "Credenciais de acesso")
public record LoginRequest(

        @Schema(description = "E-mail cadastrado", example = "ana@lumiere.dev")
        @NotBlank(message = "Email é obrigatório")
        @Email(message = "Email inválido")
        String email,

        @Schema(description = "Senha da conta", example = "senha12345")
        @NotBlank(message = "Senha é obrigatória")
        String senha
) {
}
