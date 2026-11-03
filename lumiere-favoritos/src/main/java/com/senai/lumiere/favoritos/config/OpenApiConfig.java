package com.senai.lumiere.favoritos.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI lumiereFavoritosOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("Lumiere — Favoritos e acompanhamentos")
                        .version("v1")
                        .description("""
                                Guarda o que cada usuário favoritou, quais streamings ele acompanha e \
                                quais títulos ele quer ser avisado quando chegarem em algum streaming.

                                **Todos os endpoints exigem token JWT** (diferente do catálogo, que é público). \
                                O token é emitido pelo `lumiere-auth` e validado aqui localmente, com o mesmo \
                                segredo compartilhado — não há chamada entre os dois serviços.

                                Para testar: pegue o token em `POST /auth/login` no `lumiere-auth` (porta 8081) \
                                e cole no botão **Authorize** acima. Cada usuário só enxerga e apaga os \
                                próprios registros.
                                """))
                .components(new Components().addSecuritySchemes("bearerAuth",
                        new SecurityScheme()
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")
                                .description("Token emitido pelo lumiere-auth (cole só o token, sem o prefixo 'Bearer').")));
    }
}
