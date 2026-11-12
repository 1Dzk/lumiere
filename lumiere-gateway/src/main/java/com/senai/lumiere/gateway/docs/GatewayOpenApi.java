package com.senai.lumiere.gateway.docs;

import com.senai.lumiere.gateway.routing.RotaResolver;
import com.senai.lumiere.gateway.routing.RotaResolver.Rota;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Contrato (formato OpenAPI) do PRÓPRIO gateway, na mesma linguagem que os serviços usam, para a página
 * de documentação tratar o gateway como mais uma aba. A tabela de rotas vem do {@link RotaResolver},
 * a mesma que decide o encaminhamento, então a documentação nunca diverge do comportamento.
 */
@Component
public class GatewayOpenApi {

    private final RotaResolver rotaResolver;
    private final String origemCors;
    private final long leituraMs;

    public GatewayOpenApi(
            RotaResolver rotaResolver,
            @Value("${cors.allowed-origin:*}") String origemCors,
            @Value("${gateway.timeout.read-ms:30000}") long leituraMs
    ) {
        this.rotaResolver = rotaResolver;
        this.origemCors = origemCors;
        this.leituraMs = leituraMs;
    }

    public Map<String, Object> contrato() {
        Map<String, Object> contrato = new LinkedHashMap<>();
        contrato.put("openapi", "3.0.1");
        contrato.put("info", Map.of(
                "title", "Lumiere — Gateway",
                "version", "v1",
                "description", descricao()));
        contrato.put("tags", List.of(Map.of(
                "name", "Gateway",
                "description", "O que o próprio gateway responde. Todo o resto é repassado para os serviços.")));
        contrato.put("paths", Map.of("/actuator/health", saude()));
        contrato.put("components", Map.of("schemas", Map.of("Erro", erro())));
        return contrato;
    }

    private String descricao() {
        String rotas = rotaResolver.rotas().stream().map(this::linhaDaRota).collect(Collectors.joining("\n"));

        return """
                Porta de entrada do Lumiere: recebe todas as chamadas do app e repassa para o serviço certo, \
                **sem alterar** o que o serviço responde (status, cabeçalhos e corpo).

                ## Para onde cada rota vai

                %s

                Qualquer outro caminho devolve `404`.

                ## Como os erros funcionam

                Os erros que o **próprio gateway** produz usam o formato `Erro`: `timestamp`, `status` e `mensagem`. \
                Os erros dos serviços (400, 401, 404, 409...) passam direto, no formato de cada serviço.

                - `400` — URL inválida (por exemplo, um `%%` sem dois dígitos depois dele)
                - `404` — nenhum serviço atende o caminho pedido
                - `500` — erro inesperado do gateway; o detalhe fica só no log
                - `502` — o serviço respondeu, mas o gateway não conseguiu repassar a resposta
                - `503` — o serviço está fora do ar (recusou a conexão)
                - `504` — o serviço não respondeu em %s

                ## CORS

                Origem permitida: `%s`. Em produção, restrinja com a variável `CORS_ALLOWED_ORIGIN`.

                ## Documentação dos serviços

                As abas no menu da esquerda mostram o contrato de cada serviço, lido ao vivo pelo gateway, e o botão \
                de teste chama tudo por aqui, que é o caminho real que o app usa. Se um serviço estiver fora do ar, \
                só a aba dele avisa.
                """.formatted(rotas, tempoLegivel(leituraMs), origemCors);
    }

    /** 30000 vira "30 segundos"; valores que não fecham em segundos ficam em ms. */
    static String tempoLegivel(long ms) {
        return ms >= 1000 && ms % 1000 == 0 ? (ms / 1000) + " segundos" : ms + " ms";
    }

    private String linhaDaRota(Rota rota) {
        String prefixos = rota.prefixos().stream().map(p -> "`" + p + "/**`").collect(Collectors.joining(", "));
        return "- " + prefixos + " — **" + rota.rotulo() + "**: " + rota.descricao();
    }

    private static Map<String, Object> saude() {
        Map<String, Object> resposta200 = Map.of(
                "description", "O gateway está de pé",
                "content", Map.of("application/json", Map.of("schema", Map.of(
                        "type", "object",
                        "properties", Map.of("status", Map.of("type", "string", "example", "UP"))))));

        Map<String, Object> get = new LinkedHashMap<>();
        get.put("tags", List.of("Gateway"));
        get.put("summary", "Saúde do gateway");
        get.put("description", "Responde só pelo gateway: não consulta os serviços atrás dele, então `UP` aqui "
                + "não garante que os serviços estejam de pé.");
        get.put("operationId", "saude");
        get.put("responses", Map.of("200", resposta200));
        return Map.of("get", get);
    }

    private static Map<String, Object> erro() {
        Map<String, Object> propriedades = new LinkedHashMap<>();
        propriedades.put("timestamp", Map.of("type", "string", "format", "date-time",
                "description", "Momento em que o erro aconteceu", "example", "2026-11-03T18:25:43.511Z"));
        propriedades.put("status", Map.of("type", "integer", "format", "int32",
                "description", "Código HTTP da resposta", "example", 503));
        propriedades.put("mensagem", Map.of("type", "string",
                "description", "Explicação do que deu errado", "example", "Serviço indisponível no momento para: /catalogo/em-alta"));

        Map<String, Object> erro = new LinkedHashMap<>();
        erro.put("type", "object");
        erro.put("description", "Formato dos erros que o próprio gateway devolve");
        erro.put("properties", propriedades);
        return erro;
    }
}
