package com.senai.lumiere.auth.service;

import com.senai.lumiere.auth.dto.LoginRequest;
import com.senai.lumiere.auth.dto.LoginResponse;
import com.senai.lumiere.auth.dto.mapper.UsuarioMapper;
import com.senai.lumiere.auth.exception.CredenciaisInvalidasException;
import com.senai.lumiere.auth.model.Usuario;
import com.senai.lumiere.auth.repository.UsuarioRepository;
import com.senai.lumiere.auth.security.JwtService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UsuarioRepository repository;

    @Spy
    private UsuarioMapper mapper = new UsuarioMapper();

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @InjectMocks
    private AuthService authService;

    @Test
    void login_deveRetornarTokenQuandoCredenciaisValidas() {
        Usuario usuario = new Usuario();
        usuario.setId(1L);
        usuario.setNome("Ana");
        usuario.setEmail("ana@lumiere.dev");
        usuario.setSenha("hash-da-senha");

        when(repository.findByEmail("ana@lumiere.dev")).thenReturn(Optional.of(usuario));
        when(passwordEncoder.matches("senha12345", "hash-da-senha")).thenReturn(true);
        when(jwtService.gerarToken(usuario)).thenReturn("token-fake");

        LoginResponse response = authService.login(new LoginRequest("ana@lumiere.dev", "senha12345"));

        assertThat(response.token()).isEqualTo("token-fake");
        assertThat(response.usuario().email()).isEqualTo("ana@lumiere.dev");
    }

    @Test
    void login_deveLancarExcecaoQuandoEmailNaoExiste() {
        when(repository.findByEmail("fantasma@lumiere.dev")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.login(new LoginRequest("fantasma@lumiere.dev", "qualquer")))
                .isInstanceOf(CredenciaisInvalidasException.class);
    }

    @Test
    void login_deveLancarExcecaoQuandoSenhaIncorreta() {
        Usuario usuario = new Usuario();
        usuario.setEmail("ana@lumiere.dev");
        usuario.setSenha("hash-da-senha");

        when(repository.findByEmail("ana@lumiere.dev")).thenReturn(Optional.of(usuario));
        when(passwordEncoder.matches("errada", "hash-da-senha")).thenReturn(false);

        assertThatThrownBy(() -> authService.login(new LoginRequest("ana@lumiere.dev", "errada")))
                .isInstanceOf(CredenciaisInvalidasException.class);
    }
}
