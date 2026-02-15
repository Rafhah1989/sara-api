package com.sara.api.controller;

import com.sara.api.dto.TabelaFreteRequestDTO;
import com.sara.api.dto.TabelaFreteResponseDTO;
import com.sara.api.service.TabelaFreteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tabelas-frete")
public class TabelaFreteController {

    @Autowired
    private TabelaFreteService service;

    @GetMapping
    public List<TabelaFreteResponseDTO> listarTodas() {
        return service.listarTodas();
    }

    @GetMapping("/{id}")
    public ResponseEntity<TabelaFreteResponseDTO> buscarPorId(@PathVariable Long id) {
        return service.buscarPorId(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/descricao/{descricao}")
    public List<TabelaFreteResponseDTO> buscarPorDescricao(@PathVariable String descricao) {
        return service.buscarPorDescricao(descricao);
    }

    @PostMapping
    public TabelaFreteResponseDTO cadastrar(@RequestBody TabelaFreteRequestDTO request) {
        return service.cadastrar(request);
    }

    @PutMapping("/{id}")
    public ResponseEntity<TabelaFreteResponseDTO> alterar(@PathVariable Long id,
            @RequestBody TabelaFreteRequestDTO request) {
        return ResponseEntity.ok(service.alterar(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> desativar(@PathVariable Long id) {
        service.desativar(id);
        return ResponseEntity.noContent().build();
    }
}
