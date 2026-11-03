package com.senai.lumiere.auth.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI lumiereAuthOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("Lumiere — Identidade e acesso")
                        .version("v1")
                        .description("""
                                Cadastro, login e emissão do token JWT do Lumiere.

                                O token devolvido no login é assinado com um segredo compartilhado (`JWT_SECRET`). \
                                O `lumiere-favoritos` valida esse mesmo token localmente, sem chamar este serviço \
                                em runtime — por isso os dois precisam enxergar o mesmo segredo.

                                Para testar um endpoint protegido aqui: faça `POST /auth/login`, copie o `token` \
                                da resposta e cole no botão **Authorize** acima.
                                """))
                .components(new Components().addSecuritySchemes("bearerAuth",
                        new SecurityScheme()
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")
                                .description("Token devolvido por POST /auth/login (cole só o token, sem o prefixo 'Bearer').")));
    }
}
