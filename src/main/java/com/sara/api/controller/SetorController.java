package com.sara.api.controller;

import com.sara.api.dto.SetorRequestDTO;
import com.sara.api.dto.SetorResponseDTO;
import com.sara.api.service.SetorService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/setores")
@Tag(name = "Setores", description = "Endpoints para gestão de setores e vínculos com frete")
public class SetorController {

    @Autowired
    private SetorService service;

    @GetMapping
    @Operation(summary = "Listar todos os setores", description = "Retorna todos os setores cadastrados e suas tabelas de frete vinculadas")
    public List<SetorResponseDTO> listarTodos() {
        return service.listarTodos();
    }

    @GetMapping("/descricao/{descricao}")
    public List<SetorResponseDTO> buscarPorDescricao(@PathVariable("descricao") String descricao) {
        return service.buscarPorDescricao(descricao);
    }

    @GetMapping("/{id}")
    public ResponseEntity<SetorResponseDTO> buscarPorId(@PathVariable("id") Long id) {
        return service.buscarPorId(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    @Operation(summary = "Cadastrar novo setor", description = "Cria um novo setor e vincula as tabelas de frete informadas")
    public SetorResponseDTO cadastrar(@RequestBody SetorRequestDTO request) {
        return service.cadastrar(request);
    }

    @PutMapping("/{id}")
    public ResponseEntity<SetorResponseDTO> alterar(@PathVariable("id") Long id, @RequestBody SetorRequestDTO request) {
        return ResponseEntity.ok(service.alterar(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> desativar(@PathVariable("id") Long id) {
        service.desativar(id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/ativar/{id}")
    @Operation(summary = "Ativar setor", description = "Reativa um setor que estava inativo")
    public ResponseEntity<Void> ativar(@PathVariable("id") Long id) {
        service.ativar(id);
        return ResponseEntity.ok().build();
    }

}
