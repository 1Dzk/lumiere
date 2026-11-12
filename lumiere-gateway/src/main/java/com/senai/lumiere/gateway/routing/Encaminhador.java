package com.senai.lumiere.gateway.routing;

import com.senai.lumiere.gateway.exception.RespostaInvalidaDoServicoException;
import com.senai.lumiere.gateway.exception.ServicoIndisponivelException;
import com.senai.lumiere.gateway.exception.ServicoNaoRespondeuException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.net.SocketTimeoutException;
import java.net.URI;
import java.net.http.HttpTimeoutException;
import java.util.HashSet;
import java.util.Set;

/**
 * Faz a chamada ao serviço e devolve a resposta dele sem alterar (status, cabeçalhos e corpo). Se a chamada
 * em si falhar, traduz a falha numa exceção do gateway: é a borda onde o erro de rede vira erro HTTP.
 */
@Component
@RequiredArgsConstructor
public class Encaminhador {

    /**
     * Cabeçalhos "salto a salto" (RFC 9110): valem só para a conexão entre dois nós vizinhos e não podem
     * ser repassados por um proxy. Repassar "Transfer-Encoding" ou "Connection" corrompe a resposta.
     */
    private static final Set<String> SALTO_A_SALTO = Set.of(
            "connection", "keep-alive", "proxy-authenticate", "proxy-authorization",
            "te", "trailer", "transfer-encoding", "upgrade");

    /** Na ida, além dos salto-a-salto, o cliente HTTP recalcula estes sozinho a partir do destino e do corpo. */
    private static final Set<String> NAO_REPASSAR_NA_IDA = uniao(SALTO_A_SALTO, Set.of("host", "content-length", "expect"));

    private final RestClient restClient;

    public ResponseEntity<byte[]> encaminhar(
            HttpMethod metodo, URI destino, HttpHeaders cabecalhos, byte[] corpo, String caminhoOriginal) {

        // URI pronta, não texto: o RestClient trata texto como "template" e codificaria de novo o que já vem codificado
        RestClient.RequestBodySpec requisicao = restClient.method(metodo).uri(destino);

        cabecalhos.forEach((nome, valores) -> {
            if (!NAO_REPASSAR_NA_IDA.contains(nome.toLowerCase())) {
                requisicao.header(nome, valores.toArray(new String[0]));
            }
        });

        if (corpo != null && corpo.length > 0) {
            requisicao.body(corpo);
        }

        try {
            ResponseEntity<byte[]> resposta = requisicao.retrieve()
                    .onStatus(HttpStatusCode::isError, (req, res) -> { }) // 4xx/5xx do serviço são repassados, não são falha do gateway
                    .toEntity(byte[].class);

            return ResponseEntity.status(resposta.getStatusCode())
                    .headers(semSaltoASalto(resposta.getHeaders()))
                    .body(resposta.getBody());
        } catch (RestClientException ex) {
            throw traduzir(caminhoOriginal, ex);
        }
    }

    private static HttpHeaders semSaltoASalto(HttpHeaders originais) {
        HttpHeaders limpos = new HttpHeaders();
        originais.forEach((nome, valores) -> {
            if (!SALTO_A_SALTO.contains(nome.toLowerCase())) {
                limpos.addAll(nome, valores);
            }
        });
        return limpos;
    }

    /**
     * 504: o serviço não respondeu a tempo. 503: não foi possível falar com ele (recusou a conexão, nome não
     * encontrado). 502: ele respondeu, mas a resposta não pôde ser lida.
     */
    private static RuntimeException traduzir(String caminho, RestClientException ex) {
        if (causadoPor(ex, HttpTimeoutException.class) || causadoPor(ex, SocketTimeoutException.class)) {
            return new ServicoNaoRespondeuException(caminho, ex);
        }
        if (ex instanceof ResourceAccessException) {
            return new ServicoIndisponivelException(caminho, ex);
        }
        return new RespostaInvalidaDoServicoException(caminho, ex);
    }

    private static boolean causadoPor(Throwable erro, Class<? extends Throwable> tipo) {
        for (Throwable atual = erro; atual != null; atual = atual.getCause()) {
            if (tipo.isInstance(atual)) {
                return true;
            }
        }
        return false;
    }

    private static Set<String> uniao(Set<String> a, Set<String> b) {
        Set<String> todos = new HashSet<>(a);
        todos.addAll(b);
        return Set.copyOf(todos);
    }
}
