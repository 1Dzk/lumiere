package com.senai.lumiere.auth;

import com.senai.lumiere.auth.model.Usuario;
import com.senai.lumiere.auth.security.JwtService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Prova o contrato de erro do serviço de ponta a ponta: Tomcat de verdade, cadeia real do Spring Security.
 * <p>
 * Não usa MockMvc de propósito: o MockMvc não faz o redirecionamento interno para {@code /error} que o
 * container faz em produção, e é justamente aí que o Spring Security costuma transformar o status real
 * em 401.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class AuthApiErrosTest {

    private static final String SENHA_VALIDA = "senha12345";

    @Value("${local.server.port}")
    private int porta;

    @Autowired
    private JwtService jwtService;

    private final HttpClient http = HttpClient.newHttpClient();

    private record Resposta(int status, String corpo, String contentType) {
    }

    private Resposta enviar(String metodo, String caminho, String contentType, String corpo, String token) throws Exception {
        HttpRequest.Builder req = HttpRequest.newBuilder(URI.create("http://localhost:" + porta + caminho));
        req.method(metodo, corpo == null
                ? HttpRequest.BodyPublishers.noBody()
                : HttpRequest.BodyPublishers.ofString(corpo, StandardCharsets.UTF_8));
        if (contentType != null) {
            req.header("Content-Type", contentType);
        }
        if (token != null) {
            req.header("Authorization", "Bearer " + token);
        }
        HttpResponse<byte[]> res = http.send(req.build(), HttpResponse.BodyHandlers.ofByteArray());
        // decodifica como UTF-8 de propósito: se o servidor escrever em outro charset, o acento aparece quebrado
        return new Resposta(res.statusCode(), new String(res.body(), StandardCharsets.UTF_8),
                res.headers().firstValue("Content-Type").orElse(""));
    }

    private Resposta postJson(String caminho, String json) throws Exception {
        return enviar("POST", caminho, "application/json", json, null);
    }

    private static String registroJson(String nome, String email, String senha) {
        return "{\"nome\":\"%s\",\"email\":\"%s\",\"senha\":\"%s\"}".formatted(nome, email, senha);
    }

    private static String loginJson(String email, String senha) {
        return "{\"email\":\"%s\",\"senha\":\"%s\"}".formatted(email, senha);
    }

    private static String emailNovo() {
        return "teste-" + UUID.randomUUID() + "@lumiere.dev";
    }

    private String tokenDe(String email) throws Exception {
        Resposta login = postJson("/auth/login", loginJson(email, SENHA_VALIDA));
        assertThat(login.status()).as("login para obter token: %s", login.corpo()).isEqualTo(200);
        String corpo = login.corpo();
        int inicio = corpo.indexOf("\"token\":\"") + 9;
        return corpo.substring(inicio, corpo.indexOf('"', inicio));
    }

    private static void assertFormatoDeErro(Resposta r, int statusEsperado) {
        assertThat(r.status()).as("corpo: %s", r.corpo()).isEqualTo(statusEsperado);
        assertThat(r.contentType()).contains("application/json");
        assertThat(r.corpo()).contains("\"status\":" + statusEsperado);
        assertThat(r.corpo()).contains("\"timestamp\":");
        assertThat(r.corpo()).contains("\"mensagem\":");
    }

    // ---- 400: validação ----

    @Test
    void registro_comCorpoVazio_deveRetornar400ComTodosOsCampos() throws Exception {
        Resposta r = postJson("/auth/registro", "{}");

        assertFormatoDeErro(r, 400);
        assertThat(r.corpo()).contains("\"erros\":{").contains("\"nome\":").contains("\"email\":").contains("\"senha\":");
    }

    @Test
    void registro_comSenhaVazia_deveInformarTodasAsRegrasQueFalharam() throws Exception {
        Resposta r = postJson("/auth/registro", registroJson("Ana", emailNovo(), ""));

        assertFormatoDeErro(r, 400);
        assertThat(r.corpo()).contains("Senha é obrigatória").contains("Senha deve ter no mínimo 8 caracteres");
        // cada regra aparece uma vez só (o merge não pode duplicar a mesma violação)
        assertThat(r.corpo().split("Senha é obrigatória", -1)).hasSize(2);
        assertThat(r.corpo().split("Senha deve ter no mínimo 8 caracteres", -1)).hasSize(2);
    }

    @Test
    void registro_comSenhaComAcentosAcimaDe72Bytes_deveRetornar400EmVezDeEstourar() throws Exception {
        // 60 caracteres passariam num @Size(max = 72), mas "é" ocupa 2 bytes em UTF-8: 120 bytes, acima do limite do BCrypt
        Resposta r = postJson("/auth/registro", registroJson("Ana", emailNovo(), "é".repeat(60)));

        assertFormatoDeErro(r, 400);
        assertThat(r.corpo()).contains("\"senha\":").contains("72 bytes");
    }

    @Test
    void login_comSenhaAcimaDe72Bytes_deveRetornar400() throws Exception {
        assertFormatoDeErro(postJson("/auth/login", loginJson(emailNovo(), "é".repeat(60))), 400);
    }

    @Test
    void registro_comNomeAcimaDoLimiteDaColuna_deveRetornar400() throws Exception {
        Resposta r = postJson("/auth/registro", registroJson("a".repeat(300), emailNovo(), SENHA_VALIDA));

        assertFormatoDeErro(r, 400);
        assertThat(r.corpo()).contains("\"nome\":");
    }

    @Test
    void registro_comSenhaAcimaDoLimiteDoBcrypt_deveRetornar400() throws Exception {
        Resposta r = postJson("/auth/registro", registroJson("Ana", emailNovo(), "x".repeat(100)));

        assertFormatoDeErro(r, 400);
        assertThat(r.corpo()).contains("\"senha\":");
    }

    // ---- erros do Spring MVC no nosso formato (e não mascarados em 401) ----

    @Test
    void login_comJsonQuebrado_deveRetornar400() throws Exception {
        assertFormatoDeErro(postJson("/auth/login", "{"), 400);
    }

    @Test
    void login_semCorpo_deveRetornar400() throws Exception {
        assertFormatoDeErro(enviar("POST", "/auth/login", "application/json", null, null), 400);
    }

    @Test
    void login_comContentTypeErrado_deveRetornar415() throws Exception {
        assertFormatoDeErro(enviar("POST", "/auth/login", "text/plain", "oi", null), 415);
    }

    @Test
    void login_comMetodoErrado_deveRetornar405() throws Exception {
        assertFormatoDeErro(enviar("GET", "/auth/login", null, null, null), 405);
    }

    // ---- 409: e-mail repetido ----

    @Test
    void registro_comEmailRepetido_deveRetornar409() throws Exception {
        String email = emailNovo();
        assertThat(postJson("/auth/registro", registroJson("Ana", email, SENHA_VALIDA)).status()).isEqualTo(201);

        assertFormatoDeErro(postJson("/auth/registro", registroJson("Ana", email, SENHA_VALIDA)), 409);
    }

    @Test
    void registro_comEmailQueSoDifereNaCaixa_deveRetornar409() throws Exception {
        String email = emailNovo();
        assertThat(postJson("/auth/registro", registroJson("Ana", email, SENHA_VALIDA)).status()).isEqualTo(201);

        assertFormatoDeErro(postJson("/auth/registro", registroJson("Ana", "  " + email.toUpperCase() + " ", SENHA_VALIDA)), 409);
    }

    @Test
    void login_comEmailEmCaixaDiferenteDoCadastro_deveFuncionar() throws Exception {
        String email = emailNovo();
        assertThat(postJson("/auth/registro", registroJson("Ana", email, SENHA_VALIDA)).status()).isEqualTo(201);

        assertThat(postJson("/auth/login", loginJson(email.toUpperCase(), SENHA_VALIDA)).status()).isEqualTo(200);
    }

    // ---- 401: login ----

    @Test
    void login_comSenhaErradaOuEmailInexistente_deveRetornar401ComAMesmaMensagem() throws Exception {
        String email = emailNovo();
        assertThat(postJson("/auth/registro", registroJson("Ana", email, SENHA_VALIDA)).status()).isEqualTo(201);

        Resposta senhaErrada = postJson("/auth/login", loginJson(email, "senha-errada-123"));
        Resposta emailInexistente = postJson("/auth/login", loginJson(emailNovo(), SENHA_VALIDA));

        assertFormatoDeErro(senhaErrada, 401);
        assertFormatoDeErro(emailInexistente, 401);
        assertThat(mensagemDe(senhaErrada)).isEqualTo(mensagemDe(emailInexistente));
    }

    // ---- 401: token (Spring Security) ----

    @Test
    void me_semToken_deveRetornar401NoFormatoDeErroComAcentoCorreto() throws Exception {
        Resposta r = enviar("GET", "/auth/me", null, null, null);

        assertFormatoDeErro(r, 401);
        assertThat(r.corpo()).contains("Token ausente, expirado ou inválido");
    }

    @Test
    void me_comTokenAdulterado_deveRetornar401() throws Exception {
        assertFormatoDeErro(enviar("GET", "/auth/me", null, null, "abc.def.ghi"), 401);
    }

    @Test
    void me_comTokenValido_deveRetornar200() throws Exception {
        String email = emailNovo();
        assertThat(postJson("/auth/registro", registroJson("Ana", email, SENHA_VALIDA)).status()).isEqualTo(201);

        Resposta r = enviar("GET", "/auth/me", null, null, tokenDe(email));

        assertThat(r.status()).isEqualTo(200);
        assertThat(r.corpo()).contains(email);
    }

    @Test
    void me_comTokenValidoDeUsuarioQueNaoExisteMais_deveRetornar401() throws Exception {
        Usuario fantasma = new Usuario();
        fantasma.setId(999_999L);
        fantasma.setNome("Fantasma");
        fantasma.setEmail(emailNovo());

        Resposta r = enviar("GET", "/auth/me", null, null, jwtService.gerarToken(fantasma));

        assertFormatoDeErro(r, 401);
        assertThat(r.corpo()).contains("Sessão inválida");
    }

    // ---- documentação (página /docs): pública, sem token ----

    @Test
    void docs_deveServirAPaginaSemExigirToken() throws Exception {
        Resposta pagina = enviar("GET", "/docs/index.html", null, null, null);

        assertThat(pagina.status()).isEqualTo(200);
        assertThat(pagina.contentType()).contains("text/html");
        assertThat(pagina.corpo()).contains("Lumiere");
        assertThat(enviar("GET", "/docs/lumiere-docs.js", null, null, null).status()).isEqualTo(200);
        assertThat(enviar("GET", "/docs/lumiere-docs.css", null, null, null).status()).isEqualTo(200);
    }

    @Test
    void docs_atalhoSemBarraDeveRedirecionarParaAPagina() throws Exception {
        // o HttpClient não segue redirecionamento por padrão, então o 302 aparece
        assertThat(enviar("GET", "/docs", null, null, null).status()).isEqualTo(302);
    }

    @Test
    void contratoOpenApi_deveSerPublicoEListarOsEndpointsNaOrdemDeLeitura() throws Exception {
        Resposta contrato = enviar("GET", "/v3/api-docs", null, null, null);

        assertThat(contrato.status()).isEqualTo(200);
        String json = contrato.corpo();
        int registro = json.indexOf("\"/auth/registro\"");
        int login = json.indexOf("\"/auth/login\"");
        int me = json.indexOf("\"/auth/me\"");
        assertThat(registro).isPositive();
        assertThat(registro).isLessThan(login);
        assertThat(login).isLessThan(me);
        assertThat(json).contains("Como os erros funcionam").contains("Como autenticar");
        // é o que a página usa para mostrar "requer token" e o campo de token no testador
        assertThat(json).contains("\"security\":[{\"bearerAuth\":[]}]");
    }

    // ---- rotas desconhecidas ----

    @Test
    void rotaInexistente_comTokenValido_deveRetornar404NoFormatoDeErro() throws Exception {
        String email = emailNovo();
        assertThat(postJson("/auth/registro", registroJson("Ana", email, SENHA_VALIDA)).status()).isEqualTo(201);

        assertFormatoDeErro(enviar("GET", "/auth/inexistente", null, null, tokenDe(email)), 404);
    }

    private static String mensagemDe(Resposta r) {
        int inicio = r.corpo().indexOf("\"mensagem\":\"") + 12;
        return r.corpo().substring(inicio, r.corpo().indexOf('"', inicio));
    }
}
