package com.senai.lumiere.auth.service;

import com.senai.lumiere.auth.dto.LoginRequest;
import com.senai.lumiere.auth.dto.LoginResponse;
import com.senai.lumiere.auth.dto.mapper.UsuarioMapper;
import com.senai.lumiere.auth.exception.CredenciaisInvalidasException;
import com.senai.lumiere.auth.model.Usuario;
import com.senai.lumiere.auth.repository.UsuarioRepository;
import com.senai.lumiere.auth.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UsuarioRepository repository;
    private final UsuarioMapper mapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public LoginResponse login(LoginRequest request) {
        Usuario usuario = repository.findByEmail(request.email())
                .orElseThrow(CredenciaisInvalidasException::new);

        if (!passwordEncoder.matches(request.senha(), usuario.getSenha())) {
            throw new CredenciaisInvalidasException();
        }

        String token = jwtService.gerarToken(usuario);
        return new LoginResponse(token, mapper.toResponse(usuario));
    }
}
