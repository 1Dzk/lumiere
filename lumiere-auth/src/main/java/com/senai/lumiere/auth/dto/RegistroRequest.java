package com.senai.lumiere.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "Dados para criar uma conta")
public record RegistroRequest(

        @Schema(description = "Nome do usuário", example = "Ana Souza")
        @NotBlank(message = "Nome é obrigatório")
        String nome,

        @Schema(description = "E-mail, precisa ser único", example = "ana@lumiere.dev")
        @NotBlank(message = "Email é obrigatório")
        @Email(message = "Email inválido")
        String email,

        @Schema(description = "Senha com no mínimo 8 caracteres", example = "senha12345", minLength = 8)
        @NotBlank(message = "Senha é obrigatória")
        @Size(min = 8, message = "Senha deve ter no mínimo 8 caracteres")
        String senha
) {
}
