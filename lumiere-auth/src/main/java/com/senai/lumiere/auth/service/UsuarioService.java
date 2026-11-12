package com.senai.lumiere.auth.service;

import com.senai.lumiere.auth.dto.RegistroRequest;
import com.senai.lumiere.auth.dto.UsuarioResponse;
import com.senai.lumiere.auth.dto.mapper.UsuarioMapper;
import com.senai.lumiere.auth.exception.EmailJaCadastradoException;
import com.senai.lumiere.auth.exception.SessaoInvalidaException;
import com.senai.lumiere.auth.model.Usuario;
import com.senai.lumiere.auth.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
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

        try {
            return mapper.toResponse(repository.save(usuario));
        } catch (DataIntegrityViolationException ex) {
            // O existsByEmail acima é só uma cortesia: entre ele e o save, outra requisição pode ter cadastrado
            // o mesmo e-mail. Quem garante a unicidade de verdade é o UNIQUE do banco.
            throw new EmailJaCadastradoException(request.email());
        }
    }

    public UsuarioResponse buscarPorEmail(String email) {
        // O token é válido, mas a conta pode ter sido apagada depois do login: não é bug, é sessão inválida.
        return mapper.toResponse(
                repository.findByEmail(email).orElseThrow(SessaoInvalidaException::new)
        );
    }
}
