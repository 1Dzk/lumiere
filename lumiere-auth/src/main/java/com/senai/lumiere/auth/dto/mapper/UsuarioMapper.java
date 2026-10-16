package com.senai.lumiere.auth.dto.mapper;

import com.senai.lumiere.auth.dto.RegistroRequest;
import com.senai.lumiere.auth.dto.UsuarioResponse;
import com.senai.lumiere.auth.model.Usuario;
import org.springframework.stereotype.Component;

@Component
public class UsuarioMapper {

    public UsuarioResponse toResponse(Usuario usuario) {
        if (usuario == null) {
            return null;
        }

        return new UsuarioResponse(
                usuario.getId(),
                usuario.getNome(),
                usuario.getEmail(),
                usuario.getCriadoEm()
        );
    }

    public Usuario toEntity(RegistroRequest request) {
        if (request == null) {
            return null;
        }

        Usuario usuario = new Usuario();
        usuario.setNome(request.nome());
        usuario.setEmail(request.email());
        return usuario;
    }
}
