package com.senai.lumiere.gateway.docs;

import com.senai.lumiere.gateway.exception.RotaNaoEncontradaException;
import com.senai.lumiere.gateway.routing.Encaminhador;
import com.senai.lumiere.gateway.routing.RotaResolver;
import com.senai.lumiere.gateway.routing.RotaResolver.Rota;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.MediaTypeFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import tools.jackson.databind.json.JsonMapper;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * Portal de documentação: uma página só (/docs) com uma aba por serviço, todas atendidas pela porta do gateway.
 * <p>
 * Os arquivos da página são servidos por aqui, e não pelo mecanismo padrão de recursos estáticos, porque o
 * {@code GatewayController} (que atende {@code /**}) ganharia a disputa pelo caminho {@code /docs/...}.
 */
@RestController
@RequiredArgsConstructor
public class DocsController {

    private static final String ID_DO_GATEWAY = "gateway";

    /** Só nomes simples de arquivo: nada de barra nem ".." (a pasta é a única coisa exposta). */
    private static final Pattern NOME_DE_ARQUIVO = Pattern.compile("[A-Za-z0-9][A-Za-z0-9._-]*");

    private final RotaResolver rotaResolver;
    private final Encaminhador encaminhador;
    private final GatewayOpenApi gatewayOpenApi;
    private final JsonMapper jsonMapper;

    @GetMapping({"/docs", "/docs/"})
    public ResponseEntity<Void> atalho() {
        return ResponseEntity.status(HttpStatus.FOUND).location(URI.create("/docs/index.html")).build();
    }

    @GetMapping("/docs/{arquivo:.+}")
    public ResponseEntity<Resource> arquivo(@PathVariable String arquivo) {
        Resource recurso = new ClassPathResource("static/docs/" + arquivo);
        if (!NOME_DE_ARQUIVO.matcher(arquivo).matches() || !recurso.isReadable()) {
            throw new RotaNaoEncontradaException("/docs/" + arquivo);
        }

        MediaType tipo = MediaTypeFactory.getMediaType(recurso).orElse(MediaType.APPLICATION_OCTET_STREAM);
        if ("text".equals(tipo.getType())) {
            tipo = new MediaType(tipo, StandardCharsets.UTF_8);
        }
        return ResponseEntity.ok().contentType(tipo).cacheControl(CacheControl.noCache()).body(recurso);
    }

    /** As abas da página: o gateway primeiro, depois um serviço por rota. */
    @GetMapping("/docs/specs")
    public List<Map<String, String>> fontes() {
        List<Map<String, String>> fontes = new ArrayList<>();
        fontes.add(fonte(ID_DO_GATEWAY, "Gateway"));
        rotaResolver.rotas().forEach(rota -> fontes.add(fonte(rota.id(), rota.rotulo())));
        return fontes;
    }

    /**
     * Contrato de um serviço, buscado ao vivo. Só aceita os ids da tabela de rotas (nunca uma URL vinda de
     * fora), então não dá para usar este endereço para fazer o gateway chamar qualquer lugar.
     */
    @GetMapping("/docs/specs/{servico}")
    public ResponseEntity<?> contrato(@PathVariable String servico) {
        String caminho = "/docs/specs/" + servico;

        if (ID_DO_GATEWAY.equals(servico)) {
            return ResponseEntity.ok(gatewayOpenApi.contrato());
        }
        Rota rota = rotaResolver.porId(servico).orElseThrow(() -> new RotaNaoEncontradaException(caminho));

        ResponseEntity<byte[]> resposta = encaminhador.encaminhar(
                HttpMethod.GET, URI.create(rota.url() + "/v3/api-docs"), new HttpHeaders(), null, caminho);

        if (!resposta.getStatusCode().is2xxSuccessful() || resposta.getBody() == null) {
            return resposta; // o que o serviço respondeu, sem mexer
        }
        return ResponseEntity.ok(somenteOQueOGatewayExpoe(resposta.getBody()));
    }

    /**
     * Tira do contrato os endpoints que o gateway NÃO repassa (como /interno, de uso entre serviços). A
     * página mostra o que dá para chamar pela porta do gateway, nem mais nem menos: documentar um endpoint
     * bloqueado seria anunciá-lo e ainda levar a um 404 no botão de teste.
     */
    @SuppressWarnings("unchecked")
    private Map<String, Object> somenteOQueOGatewayExpoe(byte[] contratoDoServico) {
        Map<String, Object> contrato = jsonMapper.readValue(contratoDoServico, LinkedHashMap.class);
        if (contrato.get("paths") instanceof Map<?, ?> caminhos) {
            caminhos.keySet().removeIf(caminho -> !rotaResolver.expoe(String.valueOf(caminho)));
        }
        return contrato;
    }

    private static Map<String, String> fonte(String id, String rotulo) {
        Map<String, String> fonte = new LinkedHashMap<>();
        fonte.put("id", id);
        fonte.put("rotulo", rotulo);
        fonte.put("url", "/docs/specs/" + id);
        return fonte;
    }
}
