package com.sara.api.controller;

import com.sara.api.dto.TabelaFreteRequestDTO;
import com.sara.api.dto.TabelaFreteResponseDTO;
import com.sara.api.service.TabelaFreteService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tabelas-frete")
@Tag(name = "Tabela de Fretes", description = "Endpoints para gestão das tabelas de preços de frete")
public class TabelaFreteController {

    @Autowired
    private TabelaFreteService service;

    @GetMapping
    @Operation(summary = "Listar todas as tabelas", description = "Retorna todas as tabelas de frete cadastradas")
    public List<TabelaFreteResponseDTO> listarTodas() {
        return service.listarTodas();
    }

    @GetMapping("/{id}")
    public ResponseEntity<TabelaFreteResponseDTO> buscarPorId(@PathVariable("id") Long id) {
        return service.buscarPorId(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/descricao/{descricao}")
    public List<TabelaFreteResponseDTO> buscarPorDescricao(@PathVariable("descricao") String descricao) {
        return service.buscarPorDescricao(descricao);
    }

    @PostMapping
    @Operation(summary = "Cadastrar nova tabela de frete", description = "Cria uma nova tabela de frete com descrição e valor")
    public TabelaFreteResponseDTO cadastrar(@RequestBody TabelaFreteRequestDTO request) {
        return service.cadastrar(request);
    }

    @PutMapping("/{id}")
    public ResponseEntity<TabelaFreteResponseDTO> alterar(@PathVariable("id") Long id,
            @RequestBody TabelaFreteRequestDTO request) {
        return ResponseEntity.ok(service.alterar(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> desativar(@PathVariable("id") Long id) {
        service.desativar(id);
        return ResponseEntity.noContent().build();
    }
}
