package com.senai.lumiere.notificacoes.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI lumiereNotificacoesOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("Lumiere — Notificações")
                        .version("v1")
                        .description("""
                                Serviço de fundo: a cada rodada (padrão 1 min) ele lê os desejos de todos os \
                                usuários no `lumiere-favoritos`, consulta o `lumiere-catalogo` para ver se o \
                                título já chegou em algum streaming e, quando chega, registra o aviso.

                                O envio de verdade (e-mail/push) ainda não existe — hoje o aviso vai só para o \
                                log do serviço. O endpoint abaixo serve para conferir o que já foi avisado.
                                """));
    }
}
