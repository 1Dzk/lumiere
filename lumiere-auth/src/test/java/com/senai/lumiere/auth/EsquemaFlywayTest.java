package com.senai.lumiere.auth;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * Sobe a aplicação como em produção: o Flyway cria o esquema e o Hibernate apenas <b>valida</b> que as
 * entidades batem com ele ({@code ddl-auto=validate}). Se alguém mudar uma entidade sem escrever a
 * migração correspondente, o contexto não sobe e este teste falha.
 */
@SpringBootTest(properties = {
        "spring.flyway.enabled=true",
        "spring.jpa.hibernate.ddl-auto=validate",
        "spring.datasource.url=jdbc:h2:mem:esquema_flyway;MODE=PostgreSQL;DB_CLOSE_DELAY=-1"
})
class EsquemaFlywayTest {

    @Test
    void entidadesDevemBaterComOEsquemaCriadoPeloFlyway() {
        // o teste é o próprio carregamento do contexto
    }
}
