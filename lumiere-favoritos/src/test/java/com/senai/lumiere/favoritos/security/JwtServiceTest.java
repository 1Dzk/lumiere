package com.senai.lumiere.favoritos.security;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.Test;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;

class JwtServiceTest {

    private static final String SEGREDO = "segredo-de-teste-com-pelo-menos-32-caracteres";

    private final JwtService jwtService = new JwtService(SEGREDO);

    @Test
    void extrairUsuario_deveLerIdEEmailDoToken() {
        String token = gerarTokenComSegredo(SEGREDO, 1L, "ana@lumiere.dev");

        UsuarioAutenticado usuario = jwtService.extrairUsuario(token);

        assertThat(usuario.id()).isEqualTo(1L);
        assertThat(usuario.email()).isEqualTo("ana@lumiere.dev");
    }

    @Test
    void tokenValido_deveRetornarTrueParaTokenAssinadoComMesmoSegredo() {
        String token = gerarTokenComSegredo(SEGREDO, 1L, "ana@lumiere.dev");

        assertThat(jwtService.tokenValido(token)).isTrue();
    }

    @Test
    void tokenValido_deveRetornarFalseParaTokenAssinadoComOutroSegredo() {
        String tokenComSegredoErrado = gerarTokenComSegredo(
                "outro-segredo-com-pelo-menos-32-caracteres", 1L, "ana@lumiere.dev");

        assertThat(jwtService.tokenValido(tokenComSegredoErrado)).isFalse();
    }

    private String gerarTokenComSegredo(String segredo, Long id, String email) {
        SecretKey chave = Keys.hmacShaKeyFor(segredo.getBytes(StandardCharsets.UTF_8));
        Date agora = new Date();
        return Jwts.builder()
                .subject(email)
                .claim("id", id)
                .issuedAt(agora)
                .expiration(new Date(agora.getTime() + 60_000))
                .signWith(chave)
                .compact();
    }
}
