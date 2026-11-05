package com.senai.lumiere.auth.controller;

import com.senai.lumiere.auth.dto.LoginRequest;
import com.senai.lumiere.auth.dto.LoginResponse;
import com.senai.lumiere.auth.dto.RegistroRequest;
import com.senai.lumiere.auth.dto.UsuarioResponse;
import com.senai.lumiere.auth.exception.ErroResponse;
import com.senai.lumiere.auth.service.AuthService;
import com.senai.lumiere.auth.service.UsuarioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Autenticação", description = "Cadastro, login e dados do usuário autenticado")
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UsuarioService usuarioService;
    private final AuthService authService;

    @Operation(
            summary = "Cria uma conta",
            description = "Cadastra um usuário novo. A senha é gravada com hash BCrypt e nunca volta nas respostas. Não exige autenticação."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Conta criada"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos (e-mail mal formatado, senha com menos de 8 caracteres, campo faltando)",
                    content = @Content(schema = @Schema(implementation = ErroResponse.class))),
            @ApiResponse(responseCode = "409", description = "Já existe uma conta com esse e-mail",
                    content = @Content(schema = @Schema(implementation = ErroResponse.class)))
    })
    @PostMapping("/registro")
    public ResponseEntity<UsuarioResponse> registrar(@Valid @RequestBody RegistroRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(usuarioService.registrar(request));
    }

    @Operation(
            summary = "Faz login e devolve o token JWT",
            description = "Devolve o token usado para acessar os endpoints protegidos deste serviço e do `lumiere-favoritos`. Não exige autenticação."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Login aceito, token emitido"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos",
                    content = @Content(schema = @Schema(implementation = ErroResponse.class))),
            @ApiResponse(responseCode = "401", description = "E-mail não cadastrado ou senha incorreta (a resposta é a mesma nos dois casos, de propósito)",
                    content = @Content(schema = @Schema(implementation = ErroResponse.class)))
    })
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    @Operation(
            summary = "Dados do usuário autenticado",
            description = "Devolve o usuário dono do token enviado no cabeçalho `Authorization`."
    )
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Usuário encontrado"),
            @ApiResponse(responseCode = "401", description = "Token ausente, expirado ou inválido",
                    content = @Content(schema = @Schema(implementation = ErroResponse.class)))
    })
    @GetMapping("/me")
    public ResponseEntity<UsuarioResponse> me(
            @Parameter(hidden = true) @AuthenticationPrincipal String email) {
        return ResponseEntity.ok(usuarioService.buscarPorEmail(email));
    }
}
