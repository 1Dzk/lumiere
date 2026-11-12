package com.senai.lumiere.catalogo.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * A documentação é uma página estática (static/docs) que lê o contrato em /v3/api-docs.
 * Aqui só fica o atalho: /docs leva para a página.
 */
@Configuration
public class DocsConfig implements WebMvcConfigurer {

    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        registry.addRedirectViewController("/docs", "/docs/index.html");
        registry.addRedirectViewController("/docs/", "/docs/index.html");
    }
}
