package com.sara.api.controller;

import com.sara.api.dto.OpcaoParcelamentoRequestDTO;
import com.sara.api.dto.OpcaoParcelamentoResponseDTO;
import com.sara.api.service.OpcaoParcelamentoService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/opcoes-parcelamento")
@RequiredArgsConstructor
public class OpcaoParcelamentoController {

    private final OpcaoParcelamentoService service;

    @GetMapping
    public ResponseEntity<List<OpcaoParcelamentoResponseDTO>> findAll() {
        return ResponseEntity.ok(service.findAll());
    }

    @GetMapping("/forma-pagamento/{id}")
    public ResponseEntity<List<OpcaoParcelamentoResponseDTO>> findByFormaPagamento(@PathVariable Long id) {
        return ResponseEntity.ok(service.findByFormaPagamento(id));
    }

    @GetMapping("/{id}")
    public ResponseEntity<OpcaoParcelamentoResponseDTO> findById(@PathVariable Long id) {
        return ResponseEntity.ok(service.findById(id));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<OpcaoParcelamentoResponseDTO> create(@RequestBody OpcaoParcelamentoRequestDTO request) {
        return ResponseEntity.ok(service.create(request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<OpcaoParcelamentoResponseDTO> update(@PathVariable Long id, @RequestBody OpcaoParcelamentoRequestDTO request) {
        return ResponseEntity.ok(service.update(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
