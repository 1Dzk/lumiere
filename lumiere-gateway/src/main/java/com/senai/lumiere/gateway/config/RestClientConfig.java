package com.senai.lumiere.gateway.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

import java.net.http.HttpClient;
import java.time.Duration;

@Configuration
public class RestClientConfig {

    /**
     * Sem limite de tempo, um serviço que trava segura a thread do gateway para sempre. Por isso os dois
     * limites existem e são configuráveis:
     * <ul>
     *   <li>{@code gateway.timeout.connect-ms}: quanto esperar para abrir a conexão;</li>
     *   <li>{@code gateway.timeout.read-ms}: quanto esperar pela resposta. O padrão (30 s) é folgado de propósito:
     *       o "em alta" do catálogo faz várias chamadas à TMDB quando o cache está frio.</li>
     * </ul>
     */
    @Bean
    public RestClient restClient(
            @Value("${gateway.timeout.connect-ms:3000}") long conexaoMs,
            @Value("${gateway.timeout.read-ms:30000}") long leituraMs
    ) {
        HttpClient cliente = HttpClient.newBuilder()
                // HTTP/1.1 explícito: o padrão do JDK tenta "upgrade" para h2c em toda requisição e os serviços não usam HTTP/2
                .version(HttpClient.Version.HTTP_1_1)
                // um gateway repassa o redirecionamento ao cliente, não o segue por conta própria
                .followRedirects(HttpClient.Redirect.NEVER)
                .connectTimeout(Duration.ofMillis(conexaoMs))
                .build();

        JdkClientHttpRequestFactory fabrica = new JdkClientHttpRequestFactory(cliente);
        fabrica.setReadTimeout(Duration.ofMillis(leituraMs));

        return RestClient.builder().requestFactory(fabrica).build();
    }
}
