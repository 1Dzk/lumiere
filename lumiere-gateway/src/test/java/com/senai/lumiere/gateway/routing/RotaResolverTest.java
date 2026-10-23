package com.senai.lumiere.gateway.routing;

import com.senai.lumiere.gateway.exception.RotaNaoEncontradaException;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class RotaResolverTest {

    private final RotaResolver rotaResolver = new RotaResolver(
            "http://lumiere-auth:8081",
            "http://lumiere-catalogo:8082",
            "http://lumiere-favoritos:8083",
            "http://lumiere-notificacoes:8084"
    );

    @Test
    void resolver_deveMapearAuthParaUrlDoAuth() {
        assertThat(rotaResolver.resolver("/auth/login")).isEqualTo("http://lumiere-auth:8081");
    }

    @Test
    void resolver_deveMapearCatalogoParaUrlDoCatalogo() {
        assertThat(rotaResolver.resolver("/catalogo/em-alta")).isEqualTo("http://lumiere-catalogo:8082");
    }

    @Test
    void resolver_deveMapearFavoritosStreamingsSeguidosEDesejosParaUrlDoFavoritos() {
        assertThat(rotaResolver.resolver("/favoritos")).isEqualTo("http://lumiere-favoritos:8083");
        assertThat(rotaResolver.resolver("/streamings-seguidos")).isEqualTo("http://lumiere-favoritos:8083");
        assertThat(rotaResolver.resolver("/desejos")).isEqualTo("http://lumiere-favoritos:8083");
    }

    @Test
    void resolver_deveMapearNotificacoesParaUrlDoNotificacoes() {
        assertThat(rotaResolver.resolver("/notificacoes/enviadas")).isEqualTo("http://lumiere-notificacoes:8084");
    }

    @Test
    void resolver_deveLancarExcecaoParaRotaDesconhecida() {
        assertThatThrownBy(() -> rotaResolver.resolver("/rota-que-nao-existe"))
                .isInstanceOf(RotaNaoEncontradaException.class);
    }
}
