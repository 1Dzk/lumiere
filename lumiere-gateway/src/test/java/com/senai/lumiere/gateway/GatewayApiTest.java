package com.senai.lumiere.gateway;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.Executors;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Prova o comportamento do gateway de ponta a ponta: Tomcat de verdade, um "serviço" fictício no lugar
 * dos serviços reais e uma porta fechada no lugar do que está fora do ar.
 * <p>
 * O serviço fictício responde:
 * <ul>
 *   <li>{@code /x/status/{codigo}}: devolve aquele status, com um cabeçalho e um corpo conhecidos;</li>
 *   <li>{@code /x/lento}: demora 3 s;</li>
 *   <li>{@code /x/chunked}: resposta em pedaços (Transfer-Encoding: chunked);</li>
 *   <li>qualquer outro caminho: ecoa o que recebeu (método, URI crua, cabeçalhos e corpo).</li>
 * </ul>
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class GatewayApiTest {

    private static final HttpServer SERVICO = iniciarServicoFictício();

    private static HttpServer iniciarServicoFictício() {
        try {
            HttpServer servidor = HttpServer.create(new InetSocketAddress("localhost", 0), 0);
            servidor.createContext("/", GatewayApiTest::atender);
            // o executor padrão é de UMA thread: o /lento seguraria todas as outras chamadas na fila
            servidor.setExecutor(Executors.newCachedThreadPool());
            servidor.start();
            return servidor;
        } catch (IOException ex) {
            throw new IllegalStateException(ex);
        }
    }

    @AfterAll
    static void pararServico() {
        SERVICO.stop(0);
    }

    @DynamicPropertySource
    static void propriedades(DynamicPropertyRegistry registro) {
        String servico = "http://localhost:" + SERVICO.getAddress().getPort();
        registro.add("lumiere.auth.url", () -> servico);
        registro.add("lumiere.catalogo.url", () -> servico);
        registro.add("lumiere.favoritos.url", () -> servico);
        registro.add("lumiere.notificacoes.url", () -> "http://localhost:1"); // porta fechada: serviço fora do ar
        registro.add("gateway.timeout.read-ms", () -> "800");
    }

    @Value("${local.server.port}")
    private int porta;

    private final HttpClient http = HttpClient.newHttpClient();

    private record Resposta(int status, String corpo, Map<String, String> cabecalhos) {
        String cabecalho(String nome) {
            return cabecalhos.get(nome.toLowerCase());
        }
    }

    // ---------- serviço fictício ----------

    private static void atender(HttpExchange troca) throws IOException {
        String caminho = troca.getRequestURI().getRawPath();
        try {
            if (caminho.contains("/status/")) {
                int codigo = Integer.parseInt(caminho.substring(caminho.lastIndexOf('/') + 1));
                troca.getResponseHeaders().add("X-Servico", "fake");
                responder(troca, codigo, "{\"codigo\":" + codigo + "}");
            } else if (caminho.equals("/v3/api-docs")) {
                // contrato de um serviço, com um endpoint interno (/interno) que o gateway não repassa
                responder(troca, 200, "{\"openapi\":\"3.1.0\",\"info\":{\"title\":\"Falso\"},\"paths\":{"
                        + "\"/auth/registro\":{\"post\":{}},\"/favoritos\":{\"get\":{}},\"/interno/desejos\":{\"get\":{}}}}");
            } else if (caminho.endsWith("/lento")) {
                Thread.sleep(3000);
                responder(troca, 200, "{\"lento\":true}");
            } else if (caminho.endsWith("/chunked")) {
                troca.getResponseHeaders().add("Content-Type", "application/json");
                troca.sendResponseHeaders(200, 0); // 0 = Transfer-Encoding: chunked
                try (OutputStream saida = troca.getResponseBody()) {
                    saida.write("{\"parte\":".getBytes(StandardCharsets.UTF_8));
                    saida.flush();
                    saida.write("\"completa\"}".getBytes(StandardCharsets.UTF_8));
                }
            } else {
                responder(troca, 200, eco(troca));
            }
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
        }
    }

    private static String eco(HttpExchange troca) throws IOException {
        URI uri = troca.getRequestURI();
        Map<String, String> cabecalhos = new TreeMap<>();
        troca.getRequestHeaders().forEach((nome, valores) -> cabecalhos.put(nome.toLowerCase(), String.join(",", valores)));
        String corpo = new String(troca.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);

        StringBuilder json = new StringBuilder("{\"metodo\":").append(texto(troca.getRequestMethod()))
                .append(",\"uri\":").append(texto(uri.getRawPath() + (uri.getRawQuery() != null ? "?" + uri.getRawQuery() : "")))
                .append(",\"corpo\":").append(texto(corpo))
                .append(",\"cabecalhos\":{");
        cabecalhos.forEach((nome, valor) -> json.append(texto(nome)).append(':').append(texto(valor)).append(','));
        if (!cabecalhos.isEmpty()) {
            json.setLength(json.length() - 1);
        }
        return json.append("}}").toString();
    }

    private static String texto(String valor) {
        return "\"" + valor.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n").replace("\r", "\\r") + "\"";
    }

    private static void responder(HttpExchange troca, int status, String corpo) throws IOException {
        byte[] bytes = corpo.getBytes(StandardCharsets.UTF_8);
        troca.getResponseHeaders().set("Content-Type", "application/json");
        troca.sendResponseHeaders(status, bytes.length == 0 ? -1 : bytes.length);
        try (OutputStream saida = troca.getResponseBody()) {
            saida.write(bytes);
        }
    }

    // ---------- cliente ----------

    private Resposta enviar(String metodo, String caminho, Map<String, String> cabecalhos, String corpo) throws Exception {
        HttpRequest.Builder req = HttpRequest.newBuilder(URI.create("http://localhost:" + porta + caminho));
        req.method(metodo, corpo == null
                ? HttpRequest.BodyPublishers.noBody()
                : HttpRequest.BodyPublishers.ofString(corpo, StandardCharsets.UTF_8));
        cabecalhos.forEach(req::header);
        HttpResponse<byte[]> res = http.send(req.build(), HttpResponse.BodyHandlers.ofByteArray());
        Map<String, String> recebidos = new TreeMap<>();
        res.headers().map().forEach((nome, valores) -> recebidos.put(nome.toLowerCase(), String.join(",", valores)));
        return new Resposta(res.statusCode(), new String(res.body(), StandardCharsets.UTF_8), recebidos);
    }

    private Resposta get(String caminho) throws Exception {
        return enviar("GET", caminho, Map.of(), null);
    }

    /** Fala HTTP "na mão" para enviar o que o HttpClient do JDK não deixa (cabeçalhos proibidos, URL inválida). */
    private String bruto(String requisicao, boolean lerTudo) throws Exception {
        try (Socket socket = new Socket("localhost", porta)) {
            socket.setSoTimeout(5000);
            socket.getOutputStream().write(requisicao.getBytes(StandardCharsets.UTF_8));
            socket.getOutputStream().flush();
            InputStream entrada = socket.getInputStream();
            StringBuilder lido = new StringBuilder();
            byte[] buffer = new byte[4096];
            try {
                int n;
                while ((n = entrada.read(buffer)) != -1) {
                    lido.append(new String(buffer, 0, n, StandardCharsets.UTF_8));
                    if (!lerTudo && lido.indexOf("\r\n") >= 0) {
                        break;
                    }
                }
            } catch (java.net.SocketTimeoutException ignorado) {
                // sem mais dados
            }
            return lido.toString();
        }
    }

    private static void assertFormatoDeErro(Resposta r, int statusEsperado) {
        assertThat(r.status()).as("corpo: %s", r.corpo()).isEqualTo(statusEsperado);
        assertThat(r.cabecalho("content-type")).contains("application/json");
        assertThat(r.corpo()).contains("\"status\":" + statusEsperado).contains("\"timestamp\":").contains("\"mensagem\":");
    }

    // ---------- 404: rota desconhecida ----------

    @Test
    void rotaDesconhecida_deveRetornar404NoFormatoDeErroComAcentoCorreto() throws Exception {
        Resposta r = get("/rota-que-nao-existe");

        assertFormatoDeErro(r, 404);
        assertThat(r.corpo()).contains("Nenhum serviço mapeado");
    }

    @Test
    void prefixoSemFronteira_naoDeveSerEncaminhado() throws Exception {
        // "/authors" só começa com "/auth": não é uma rota do serviço de autenticação
        assertFormatoDeErro(get("/authors"), 404);
        assertFormatoDeErro(get("/catalogoXYZ/echo"), 404);
        assertFormatoDeErro(get("/desejosfoo"), 404);
    }

    @Test
    void raizDoGateway_deveRetornar404NoFormatoDeErro() throws Exception {
        assertFormatoDeErro(get("/"), 404);
    }

    // ---------- repasse fiel do que o serviço responde ----------

    @Test
    void deveRepassarStatusCabecalhoECorpoDoServico() throws Exception {
        Resposta r = get("/auth/status/409");

        assertThat(r.status()).isEqualTo(409);
        assertThat(r.cabecalho("x-servico")).isEqualTo("fake");
        assertThat(r.corpo()).isEqualTo("{\"codigo\":409}");
    }

    @Test
    void deveRepassarErroDeServidorDoServicoSemTrocarOStatus() throws Exception {
        Resposta r = get("/catalogo/status/500");

        assertThat(r.status()).isEqualTo(500);
        assertThat(r.corpo()).isEqualTo("{\"codigo\":500}");
    }

    @Test
    void deveRepassarRespostaEmPedacosSemCorromperOCorpo() throws Exception {
        Resposta r = get("/favoritos/chunked");

        assertThat(r.status()).isEqualTo(200);
        assertThat(r.corpo()).isEqualTo("{\"parte\":\"completa\"}");
    }

    // ---------- repasse fiel do que o cliente envia ----------

    @Test
    void queryStringCodificada_deveChegarAoServicoIntacta() throws Exception {
        Resposta r = get("/catalogo/echo?titulo=am%C3%A9lie%20poulain&x=%7Ba%7D");

        assertThat(r.status()).isEqualTo(200);
        assertThat(r.corpo()).contains("\"uri\":\"/catalogo/echo?titulo=am%C3%A9lie%20poulain&x=%7Ba%7D\"");
    }

    @Test
    void caminhoCodificado_deveChegarAoServicoIntacto() throws Exception {
        Resposta r = get("/catalogo/echo/a%20b");

        assertThat(r.corpo()).contains("\"uri\":\"/catalogo/echo/a%20b\"");
    }

    @Test
    void deveRepassarCabecalhosDoCliente() throws Exception {
        Resposta r = enviar("GET", "/auth/echo", Map.of("Authorization", "Bearer abc.def.ghi", "X-Teste", "ola"), null);

        assertThat(r.corpo()).contains("\"authorization\":\"Bearer abc.def.ghi\"").contains("\"x-teste\":\"ola\"");
    }

    @Test
    void deveRepassarMetodoCorpoETipoDeConteudo() throws Exception {
        Resposta r = enviar("POST", "/auth/echo", Map.of("Content-Type", "application/json"), "{\"email\":\"ana@lumiere.dev\"}");

        assertThat(r.corpo()).contains("\"metodo\":\"POST\"")
                .contains("\"corpo\":\"{\\\"email\\\":\\\"ana@lumiere.dev\\\"}\"")
                .contains("\"content-type\":\"application/json\"");
    }

    @Test
    void cabecalhoConnectionDoCliente_naoDeveDerrubarARequisicao() throws Exception {
        // navegadores mandam "Connection: keep-alive"; o HttpClient do JDK recusa esse cabeçalho se for repassado
        String resposta = bruto("GET /catalogo/echo HTTP/1.1\r\nHost: localhost\r\nConnection: keep-alive\r\n\r\n", false);

        assertThat(resposta).startsWith("HTTP/1.1 200");
    }

    // ---------- falhas do serviço ----------

    @Test
    void servicoForaDoAr_deveRetornar503NoFormatoDeErro() throws Exception {
        Resposta r = get("/notificacoes/enviadas");

        assertFormatoDeErro(r, 503);
        assertThat(r.corpo()).contains("indisponível");
    }

    @Test
    void servicoLento_deveRetornar504NoFormatoDeErro() throws Exception {
        // o serviço fictício demora 3 s e o gateway só espera 800 ms
        Resposta r = get("/catalogo/lento");

        assertFormatoDeErro(r, 504);
    }

    // ---------- erros do próprio Tomcat/MVC no nosso formato ----------

    @Test
    void urlComPorcentagemInvalida_deveRetornar400NoFormatoDeErro() throws Exception {
        String resposta = bruto("GET /catalogo/echo?x=%ZZ HTTP/1.1\r\nHost: localhost\r\nConnection: close\r\n\r\n", true);

        assertThat(resposta).startsWith("HTTP/1.1 400");
        assertThat(resposta).contains("\"status\":400").contains("\"mensagem\":");
    }

    @Test
    void urlComCaractereIlegalNoCaminho_deveRetornar400NoFormatoDeErro() throws Exception {
        // o Tomcat recusa "{" no caminho antes de o Spring ver a requisição; o ErroController põe isso no nosso formato
        String resposta = bruto("GET /catalogo/a{b HTTP/1.1\r\nHost: localhost\r\nConnection: close\r\n\r\n", true);

        assertThat(resposta).startsWith("HTTP/1.1 400");
        assertThat(resposta).contains("\"status\":400").contains("\"mensagem\":");
    }

    @Test
    void pedirErroDiretamente_naoDeveExplicarErroNenhum() throws Exception {
        assertFormatoDeErro(get("/error"), 404);
    }

    // ---------- portal de documentação (/docs) ----------

    @Test
    void docs_deveServirAPaginaEOsArquivosDela() throws Exception {
        Resposta pagina = get("/docs/index.html");

        assertThat(pagina.status()).isEqualTo(200);
        assertThat(pagina.cabecalho("content-type")).contains("text/html").containsIgnoringCase("utf-8");
        assertThat(pagina.corpo()).contains("Lumiere").contains("/docs/specs");
        assertThat(get("/docs/lumiere-docs.js").status()).isEqualTo(200);
        assertThat(get("/docs/lumiere-docs.js").cabecalho("content-type")).containsIgnoringCase("utf-8");
        assertThat(get("/docs/lumiere-docs.css").status()).isEqualTo(200);
    }

    @Test
    void docs_atalhoDeveRedirecionarParaAPagina() throws Exception {
        Resposta r = get("/docs");

        assertThat(r.status()).isEqualTo(302);
        assertThat(r.cabecalho("location")).endsWith("/docs/index.html");
    }

    @Test
    void docs_arquivoInexistenteOuForaDaPasta_deveRetornar404() throws Exception {
        assertFormatoDeErro(get("/docs/nao-existe.js"), 404);
        assertFormatoDeErro(get("/docs/a/b.js"), 404);
        // tentativa de sair da pasta: o Tomcat recusa a barra codificada (400) antes mesmo de chegar ao gateway
        assertFormatoDeErro(get("/docs/..%2f..%2fapplication.properties"), 400);
    }

    @Test
    void docs_deveListarAbaDoGatewayEUmaPorServico() throws Exception {
        Resposta r = get("/docs/specs");

        assertThat(r.status()).isEqualTo(200);
        assertThat(r.corpo()).contains("\"id\":\"gateway\"", "\"id\":\"auth\"", "\"id\":\"catalogo\"",
                "\"id\":\"favoritos\"", "\"id\":\"notificacoes\"");
        assertThat(r.corpo().indexOf("\"id\":\"gateway\"")).isLessThan(r.corpo().indexOf("\"id\":\"auth\""));
    }

    @Test
    void docs_contratoDoGateway_deveDescreverRotasEErrosAPartirDaTabelaDeRotas() throws Exception {
        Resposta r = get("/docs/specs/gateway");

        assertThat(r.status()).isEqualTo(200);
        assertThat(r.cabecalho("content-type")).contains("application/json");
        assertThat(r.corpo()).contains("Como os erros funcionam", "Para onde cada rota vai",
                        "/auth/**", "/catalogo/**", "/favoritos/**", "/desejos/**", "/streamings-seguidos/**", "/notificacoes/**")
                .contains("/actuator/health")
                .contains("o serviço não respondeu em 800 ms"); // o limite configurado nestes testes
    }

    @Test
    void docs_contratoDeUmServico_deveSerBuscadoAoVivoNoServico() throws Exception {
        Resposta r = get("/docs/specs/auth");

        assertThat(r.status()).isEqualTo(200);
        assertThat(r.cabecalho("content-type")).contains("application/json");
        // veio do /v3/api-docs do serviço (o conteúdo "Falso" só existe lá)
        assertThat(r.corpo()).contains("\"title\":\"Falso\"");
    }

    @Test
    void docs_contratoDeUmServico_naoDeveAnunciarEndpointQueOGatewayNaoRepassa() throws Exception {
        Resposta r = get("/docs/specs/favoritos");

        assertThat(r.corpo()).contains("\"/auth/registro\"", "\"/favoritos\"");
        assertThat(r.corpo()).doesNotContain("/interno/desejos");
        // e o gateway de fato não o repassa
        assertFormatoDeErro(get("/interno/desejos"), 404);
    }

    @Test
    void docs_servicoForaDoAr_deveAvisarSoDaquelaAba() throws Exception {
        assertFormatoDeErro(get("/docs/specs/notificacoes"), 503);
        assertThat(get("/docs/specs/gateway").status()).isEqualTo(200);
    }

    @Test
    void docs_servicoDesconhecido_deveRetornar404SemChamarNingem() throws Exception {
        assertFormatoDeErro(get("/docs/specs/qualquer-coisa"), 404);
        // um endereço no lugar do id nunca vira destino de chamada: só ids da tabela de rotas são aceitos
        assertFormatoDeErro(get("/docs/specs/localhost:8081"), 404);
    }

    // ---------- CORS e o próprio gateway ----------

    @Test
    void preflightCors_deveSerRespondidoPeloGateway() throws Exception {
        Resposta r = enviar("OPTIONS", "/favoritos", Map.of(
                "Origin", "http://localhost:5173",
                "Access-Control-Request-Method", "POST",
                "Access-Control-Request-Headers", "authorization,content-type"), null);

        assertThat(r.status()).isIn(200, 204);
        assertThat(r.cabecalho("access-control-allow-origin")).isNotBlank();
        assertThat(r.cabecalho("access-control-allow-methods")).contains("POST");
    }

    @Test
    void saudeDoGateway_deveSerRespondidaPeloProprioGateway() throws Exception {
        Resposta r = get("/actuator/health");

        assertThat(r.status()).isEqualTo(200);
        assertThat(r.corpo()).contains("UP");
    }
}
