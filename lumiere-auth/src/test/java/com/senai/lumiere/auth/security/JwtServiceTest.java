package com.senai.lumiere.auth.security;

import com.senai.lumiere.auth.model.Usuario;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class JwtServiceTest {

    private static final String SEGREDO_TESTE = "segredo-de-teste-com-pelo-menos-32-caracteres";

    private final JwtService jwtService = new JwtService(SEGREDO_TESTE, 60_000);

    @Test
    void gerarTokenETokenValido_devemFuncionarEmCicloCompleto() {
        Usuario usuario = new Usuario();
        usuario.setId(1L);
        usuario.setNome("Ana");
        usuario.setEmail("ana@lumiere.dev");

        String token = jwtService.gerarToken(usuario);

        assertThat(jwtService.tokenValido(token)).isTrue();
        assertThat(jwtService.extrairEmail(token)).isEqualTo("ana@lumiere.dev");
    }

    @Test
    void tokenValido_deveRetornarFalseParaTokenAdulterado() {
        Usuario usuario = new Usuario();
        usuario.setId(1L);
        usuario.setEmail("ana@lumiere.dev");
        String token = jwtService.gerarToken(usuario);

        String tokenAdulterado = token.substring(0, token.length() - 2) + "xx";

        assertThat(jwtService.tokenValido(tokenAdulterado)).isFalse();
    }

    @Test
    void tokenValido_deveRetornarFalseParaTokenExpirado() throws InterruptedException {
        JwtService jwtServiceComExpiracaoCurta = new JwtService(SEGREDO_TESTE, 1);
        Usuario usuario = new Usuario();
        usuario.setEmail("ana@lumiere.dev");
        String token = jwtServiceComExpiracaoCurta.gerarToken(usuario);

        Thread.sleep(50);

        assertThat(jwtServiceComExpiracaoCurta.tokenValido(token)).isFalse();
    }
}
