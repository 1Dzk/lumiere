package com.senai.lumiere.auth.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Paths;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {

    /**
     * O springdoc não garante ordem entre endpoints (depende da reflexão da JVM). Esta é a ordem de
     * leitura da documentação, que segue o caminho do usuário: criar conta, entrar, usar o token.
     * Endpoint novo que não esteja aqui entra no final.
     */
    private static final List<String> ORDEM_DOS_ENDPOINTS = List.of(
            "/auth/registro",
            "/auth/login",
            "/auth/me"
    );

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

                                ## Como autenticar

                                - Crie a conta com `POST /auth/registro` (o e-mail é guardado em minúsculas, sem espaços nas pontas)
                                - Entre com `POST /auth/login` e copie o `token` da resposta
                                - Envie o token no cabeçalho `Authorization: Bearer <token>` nos endpoints marcados como protegidos
                                - O token vale 24 horas por padrão; depois disso, faça login de novo

                                Nesta página, o login guarda o token na aba e já preenche o campo dos endpoints protegidos.

                                ## Como os erros funcionam

                                Toda falha volta no mesmo formato (o modelo `Erro`): `timestamp`, `status`, \
                                `mensagem` e, nos erros de validação, `erros` com campo → motivo.

                                - `400` — corpo ausente ou JSON inválido, ou campo inválido (nome ou e-mail com mais de 255 caracteres, senha fora do limite...); o campo `erros` diz qual e por quê
                                - `401` — login com e-mail inexistente ou senha errada (a resposta é a mesma nos dois casos, de propósito), token ausente, expirado ou inválido, ou conta dona do token que não existe mais
                                - `405` — método HTTP não permitido para o endereço
                                - `409` — já existe uma conta com esse e-mail
                                - `415` — o corpo precisa ser enviado como `application/json`
                                - `500` — erro inesperado do serviço; o detalhe fica só no log
                                """))
                .components(new Components().addSecuritySchemes("bearerAuth",
                        new SecurityScheme()
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")
                                .description("Token devolvido por POST /auth/login (cole só o token, sem o prefixo 'Bearer').")));
    }

    @Bean
    public OpenApiCustomizer ordenarEndpoints() {
        return openApi -> {
            Paths original = openApi.getPaths();
            if (original == null) {
                return;
            }
            Paths ordenado = new Paths();
            ORDEM_DOS_ENDPOINTS.stream()
                    .filter(original::containsKey)
                    .forEach(caminho -> ordenado.addPathItem(caminho, original.get(caminho)));
            original.forEach((caminho, item) -> {
                if (!ordenado.containsKey(caminho)) {
                    ordenado.addPathItem(caminho, item);
                }
            });
            openApi.setPaths(ordenado);
        };
    }
}
