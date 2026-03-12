package com.sara.api.controller;

import com.sara.api.dto.LoginDTO;
import com.sara.api.dto.TokenDTO;
import com.sara.api.model.Usuario;
import com.sara.api.security.TokenService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
@Tag(name = "Autenticação", description = "Endpoints para Login e Logout")
public class AutenticacaoController {

    @Autowired
    private AuthenticationManager manager;

    @Autowired
    private TokenService tokenService;

    @Autowired
    private com.sara.api.service.UsuarioService usuarioService;

    @PostMapping("/login")
    @Operation(summary = "Efetuar login", description = "Autentica o usuário e retorna o token JWT")
    public ResponseEntity<TokenDTO> efetuarLogin(@RequestBody LoginDTO dados) {
        var authenticationToken = new UsernamePasswordAuthenticationToken(dados.cpfCnpj(), dados.senha());
        var authentication = manager.authenticate(authenticationToken);

        var tokenJWT = tokenService.gerarToken((Usuario) authentication.getPrincipal());

        return ResponseEntity.ok(new TokenDTO(tokenJWT));
    }

    @PostMapping("/logout")
    @Operation(summary = "Efetuar logout", description = "Invalida a sessão atual no cliente (token stateless na API)")
    public ResponseEntity<Void> efetuarLogout() {
        return ResponseEntity.ok().build();
    }

    @PostMapping("/forgot-password")
    @Operation(summary = "Solicitar recuperação de senha", description = "Inicia o processo de recuperação de senha enviando um e-mail com o token")
    public ResponseEntity<Void> forgotPassword(@RequestBody com.sara.api.dto.ForgotPasswordRequestDTO dados) {
        usuarioService.iniciarRecuperacaoSenha(dados.cpfCnpj());
        return ResponseEntity.ok().build();
    }

    @PostMapping("/reset-password")
    @Operation(summary = "Redefinir senha", description = "Altera a senha do usuário utilizando o token de recuperação")
    public ResponseEntity<Void> resetPassword(@RequestBody com.sara.api.dto.ResetPasswordRequestDTO dados) {
        usuarioService.redefinirSenha(dados.token(), dados.novaSenha());
        return ResponseEntity.ok().build();
    }
}
