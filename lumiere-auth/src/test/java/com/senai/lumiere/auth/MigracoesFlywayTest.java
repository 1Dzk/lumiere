package com.senai.lumiere.auth;

import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.output.MigrateResult;
import org.h2.jdbcx.JdbcDataSource;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * O esquema do banco é responsabilidade do Flyway (src/main/resources/db/migration), nunca do Hibernate
 * ({@code ddl-auto=validate} em produção). Este teste roda as migrações de verdade, num H2 em modo
 * PostgreSQL, para pegar migração quebrada antes de ela chegar ao Postgres.
 */
class MigracoesFlywayTest {

    private static JdbcDataSource banco(String nome) {
        JdbcDataSource ds = new JdbcDataSource();
        ds.setURL("jdbc:h2:mem:" + nome + ";MODE=PostgreSQL;DB_CLOSE_DELAY=-1");
        ds.setUser("sa");
        ds.setPassword("");
        return ds;
    }

    private static Flyway flyway(JdbcDataSource ds, String ateVersao) {
        var configuracao = Flyway.configure().dataSource(ds).locations("classpath:db/migration");
        if (ateVersao != null) {
            configuracao.target(ateVersao);
        }
        return configuracao.load();
    }

    @Test
    void todasAsMigracoesDevemSerAplicadasDoZero() {
        JdbcDataSource ds = banco("migracoes_zero");

        MigrateResult resultado = flyway(ds, null).migrate();

        assertThat(resultado.success).isTrue();
        assertThat(resultado.migrationsExecuted).isGreaterThanOrEqualTo(2);
        assertThat(flyway(ds, null).info().current().getVersion().getVersion()).isEqualTo("2");
    }

    @Test
    void v2_deveNormalizarOsEmailsQueJaExistiamNoBanco() throws Exception {
        JdbcDataSource ds = banco("migracoes_v2");

        // banco "antigo": só a V1 aplicada, com um e-mail cadastrado em caixa mista e com espaços
        flyway(ds, "1").migrate();
        try (Connection conexao = ds.getConnection(); Statement sql = conexao.createStatement()) {
            sql.execute("INSERT INTO usuarios (nome, email, senha, criado_em) "
                    + "VALUES ('Ana', '  Ana@Lumiere.DEV ', 'hash', CURRENT_TIMESTAMP)");
        }

        MigrateResult resultado = flyway(ds, null).migrate();

        assertThat(resultado.success).isTrue();
        assertThat(resultado.migrationsExecuted).isEqualTo(1);
        try (Connection conexao = ds.getConnection();
             Statement sql = conexao.createStatement();
             ResultSet rs = sql.executeQuery("SELECT email FROM usuarios")) {
            assertThat(rs.next()).isTrue();
            assertThat(rs.getString("email")).isEqualTo("ana@lumiere.dev");
        }
    }
}
