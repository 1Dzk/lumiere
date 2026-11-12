package com.senai.lumiere.gateway.config;

import com.senai.lumiere.gateway.exception.RelatorioDeErroEmJson;
import org.apache.catalina.core.StandardHost;
import org.springframework.boot.tomcat.servlet.TomcatServletWebServerFactory;
import org.springframework.boot.web.server.WebServerFactoryCustomizer;
import org.springframework.stereotype.Component;

/**
 * Troca o relatório de erro padrão do Tomcat (uma página HTML) por um em JSON no formato do contrato,
 * para que até as requisições que o Tomcat recusa sozinho respondam como o resto da API.
 */
@Component
public class TomcatErrosNoFormatoDoContrato implements WebServerFactoryCustomizer<TomcatServletWebServerFactory> {

    @Override
    public void customize(TomcatServletWebServerFactory fabrica) {
        fabrica.addContextCustomizers(contexto -> {
            if (contexto.getParent() instanceof StandardHost host) {
                host.setErrorReportValveClass(RelatorioDeErroEmJson.class.getName());
            }
        });
    }
}
