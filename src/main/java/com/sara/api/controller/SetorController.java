package com.sara.api.controller;

import com.sara.api.dto.SetorRequestDTO;
import com.sara.api.dto.SetorResponseDTO;
import com.sara.api.service.SetorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/setores")
public class SetorController {

    @Autowired
    private SetorService service;

    @GetMapping
    public List<SetorResponseDTO> listarTodos() {
        return service.listarTodos();
    }

    @GetMapping("/descricao/{descricao}")
    public List<SetorResponseDTO> buscarPorDescricao(@PathVariable String descricao) {
        return service.buscarPorDescricao(descricao);
    }

    @GetMapping("/{id}")
    public ResponseEntity<SetorResponseDTO> buscarPorId(@PathVariable Long id) {
        return service.buscarPorId(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public SetorResponseDTO cadastrar(@RequestBody SetorRequestDTO request) {
        return service.cadastrar(request);
    }

    @PutMapping("/{id}")
    public ResponseEntity<SetorResponseDTO> alterar(@PathVariable Long id, @RequestBody SetorRequestDTO request) {
        return ResponseEntity.ok(service.alterar(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> desativar(@PathVariable Long id) {
        service.desativar(id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/ativar/{id}")
    public ResponseEntity<Void> ativar(@PathVariable Long id) {
        service.ativar(id);
        return ResponseEntity.ok().build();
    }

}
