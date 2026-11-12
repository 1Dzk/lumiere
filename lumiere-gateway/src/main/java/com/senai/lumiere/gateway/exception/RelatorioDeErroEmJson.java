package com.senai.lumiere.gateway.exception;

import org.apache.catalina.connector.Request;
import org.apache.catalina.connector.Response;
import org.apache.catalina.valves.ErrorReportValve;

import java.io.IOException;
import java.io.Writer;
import java.time.Instant;

/**
 * Relatório de erro do Tomcat no formato do {@link ErroResponse}.
 * <p>
 * Existe porque o Tomcat recusa certas requisições ANTES de o Spring existir (barra codificada no caminho,
 * caractere ilegal na URL, cabeçalho grande demais) e, nesses casos, responde com a página HTML dele: nem o
 * {@code GlobalExceptionHandler} nem o {@link ErroController} chegam a rodar. Registrado em
 * {@code TomcatErrosNoFormatoDoContrato}; precisa ser público e ter construtor sem argumentos porque o
 * Tomcat o instancia pelo nome da classe.
 */
public class RelatorioDeErroEmJson extends ErrorReportValve {

    @Override
    protected void report(Request request, Response response, Throwable throwable) {
        int status = response.getStatus();
        if (status < 400 || response.getContentWritten() > 0) {
            return;
        }

        try {
            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");
            Writer escritor = response.getReporter();
            if (escritor != null) {
                escritor.write("{\"timestamp\":\"%s\",\"status\":%d,\"mensagem\":\"%s\"}"
                        .formatted(Instant.now(), status, MensagensPadrao.para(status)));
                response.finishResponse();
            }
        } catch (IOException | IllegalStateException ex) {
            // a resposta já não aceita escrita (cliente desconectou, por exemplo): não há mais o que fazer
        }
    }
}
