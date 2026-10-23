package com.senai.lumiere.auth.service;

import com.senai.lumiere.auth.dto.RegistroRequest;
import com.senai.lumiere.auth.dto.UsuarioResponse;
import com.senai.lumiere.auth.dto.mapper.UsuarioMapper;
import com.senai.lumiere.auth.exception.EmailJaCadastradoException;
import com.senai.lumiere.auth.model.Usuario;
import com.senai.lumiere.auth.repository.UsuarioRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UsuarioServiceTest {

    @Mock
    private UsuarioRepository repository;

    @Spy
    private UsuarioMapper mapper = new UsuarioMapper();

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UsuarioService usuarioService;

    @Test
    void registrar_deveSalvarUsuarioComSenhaCriptografada() {
        RegistroRequest request = new RegistroRequest("Ana", "ana@lumiere.dev", "senha12345");
        when(repository.existsByEmail("ana@lumiere.dev")).thenReturn(false);
        when(passwordEncoder.encode("senha12345")).thenReturn("hash-da-senha");
        when(repository.save(any(Usuario.class))).thenAnswer(invocation -> invocation.getArgument(0));

        UsuarioResponse response = usuarioService.registrar(request);

        ArgumentCaptor<Usuario> captor = ArgumentCaptor.forClass(Usuario.class);
        verify(repository).save(captor.capture());
        assertThat(captor.getValue().getSenha()).isEqualTo("hash-da-senha");
        assertThat(response.nome()).isEqualTo("Ana");
        assertThat(response.email()).isEqualTo("ana@lumiere.dev");
    }

    @Test
    void registrar_deveLancarExcecaoQuandoEmailJaExiste() {
        RegistroRequest request = new RegistroRequest("Ana", "ana@lumiere.dev", "senha12345");
        when(repository.existsByEmail("ana@lumiere.dev")).thenReturn(true);

        assertThatThrownBy(() -> usuarioService.registrar(request))
                .isInstanceOf(EmailJaCadastradoException.class);

        verify(repository, never()).save(any());
    }
}
