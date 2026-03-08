package com.sara.api.controller;

import com.sara.api.dto.FormaPagamentoDTO;
import com.sara.api.service.FormaPagamentoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/formas-pagamento")
@Tag(name = "Formas de Pagamento", description = "Endpoints para gerenciamento de Formas de Pagamento")
public class FormaPagamentoController {

    @Autowired
    private FormaPagamentoService formaPagamentoService;

    @Operation(summary = "Listar todas as formas de pagamento")
    @GetMapping
    public ResponseEntity<List<FormaPagamentoDTO>> listarTodos() {
        return ResponseEntity.ok(formaPagamentoService.listarTodos());
    }

    @Operation(summary = "Buscar forma de pagamento por ID")
    @GetMapping("/{id}")
    public ResponseEntity<FormaPagamentoDTO> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(formaPagamentoService.buscarPorId(id));
    }

    @Operation(summary = "Criar uma nova forma de pagamento (Apenas ADMIN)")
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<FormaPagamentoDTO> criar(@RequestBody FormaPagamentoDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(formaPagamentoService.criar(dto));
    }

    @Operation(summary = "Alterar uma forma de pagamento existente (Apenas ADMIN)")
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<FormaPagamentoDTO> alterar(@PathVariable Long id, @RequestBody FormaPagamentoDTO dto) {
        return ResponseEntity.ok(formaPagamentoService.alterar(id, dto));
    }

    @Operation(summary = "Excluir uma forma de pagamento (Apenas ADMIN)")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> excluir(@PathVariable Long id) {
        formaPagamentoService.excluir(id);
        return ResponseEntity.noContent().build();
    }
}
