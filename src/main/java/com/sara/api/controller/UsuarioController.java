package com.sara.api.controller;

import com.sara.api.dto.UsuarioRequestDTO;
import com.sara.api.dto.UsuarioResponseDTO;
import com.sara.api.service.UsuarioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/usuarios")
@Tag(name = "Usuários", description = "Endpoints para gestão de usuários do sistema")
public class UsuarioController {

    @Autowired
    private UsuarioService usuarioService;

    @PostMapping
    @Operation(summary = "Criar novo usuário", description = "Cadastra um novo usuário no sistema com senha criptografada")
    public UsuarioResponseDTO criar(@RequestBody UsuarioRequestDTO request) {
        return usuarioService.criar(request);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Alterar usuário", description = "Atualiza os dados de um usuário existente por ID")
    public ResponseEntity<UsuarioResponseDTO> alterar(@PathVariable("id") Long id,
            @RequestBody UsuarioRequestDTO request) {
        return ResponseEntity.ok(usuarioService.alterar(id, request));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Desativar usuário", description = "Marca um usuário como inativo no sistema")
    public ResponseEntity<Void> desativar(@PathVariable("id") Long id) {
        usuarioService.desativar(id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/ativar/{id}")
    @Operation(summary = "Ativar usuário", description = "Reativa um usuário que estava marcado como inativo")
    public ResponseEntity<Void> ativar(@PathVariable("id") Long id) {
        usuarioService.ativar(id);
        return ResponseEntity.ok().build();
    }

    @GetMapping
    @Operation(summary = "Listar todos os usuários", description = "Retorna uma lista de todos os usuários cadastrados")
    public List<UsuarioResponseDTO> listarTodos() {
        return usuarioService.listarTodos();
    }

    @GetMapping("/{id}")
    public ResponseEntity<UsuarioResponseDTO> buscarPorId(@PathVariable("id") Long id) {
        return usuarioService.buscarPorId(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/nome/{nome}")
    public List<UsuarioResponseDTO> buscarPorNome(@PathVariable("nome") String nome) {
        return usuarioService.buscarPorNome(nome);
    }

    @GetMapping("/telefone/{telefone}")
    public List<UsuarioResponseDTO> buscarPorTelefone(@PathVariable("telefone") String telefone) {
        return usuarioService.buscarPorTelefone(telefone);
    }

    @PostMapping("/reenviar-convite/{id}")
    @Operation(summary = "Reenviar e-mail de convite", description = "Gera nova senha e token e reenvia o e-mail de boas-vindas")
    public ResponseEntity<Void> reenviarEmailConvite(@PathVariable("id") Long id) {
        usuarioService.reenviarEmailConvite(id);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/validate-token")
    @Operation(summary = "Validar token de senha", description = "Verifica se um token de redefinição ainda é válido")
    public ResponseEntity<UsuarioResponseDTO> validarToken(@RequestBody String token) {
        // Remove aspas se o token vier como string JSON pura
        token = token.replace("\"", ""); 
        return ResponseEntity.ok(usuarioService.validarToken(token));
    }

    @PostMapping("/reset-password")
    @Operation(summary = "Redefinir senha", description = "Altera a senha do usuário utilizando um token válido")
    public ResponseEntity<Void> redefinirSenha(@RequestBody ResetPasswordRequest request) {
        usuarioService.redefinirSenha(request.token(), request.novaSenha());
        return ResponseEntity.ok().build();
    }

    public record ResetPasswordRequest(String token, String novaSenha) {}
}
