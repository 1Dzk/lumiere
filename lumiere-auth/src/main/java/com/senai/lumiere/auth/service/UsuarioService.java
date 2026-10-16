package com.senai.lumiere.auth.service;

import com.senai.lumiere.auth.dto.RegistroRequest;
import com.senai.lumiere.auth.dto.UsuarioResponse;
import com.senai.lumiere.auth.dto.mapper.UsuarioMapper;
import com.senai.lumiere.auth.exception.EmailJaCadastradoException;
import com.senai.lumiere.auth.model.Usuario;
import com.senai.lumiere.auth.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UsuarioService {

    private final UsuarioRepository repository;
    private final UsuarioMapper mapper;
    private final PasswordEncoder passwordEncoder;

    public UsuarioResponse registrar(RegistroRequest request) {
        if (repository.existsByEmail(request.email())) {
            throw new EmailJaCadastradoException(request.email());
        }

        Usuario usuario = mapper.toEntity(request);
        usuario.setSenha(passwordEncoder.encode(request.senha()));

        return mapper.toResponse(repository.save(usuario));
    }

    public UsuarioResponse buscarPorEmail(String email) {
        return mapper.toResponse(
                repository.findByEmail(email)
                        .orElseThrow(() -> new IllegalStateException("Usuário autenticado não encontrado: " + email))
        );
    }
}
