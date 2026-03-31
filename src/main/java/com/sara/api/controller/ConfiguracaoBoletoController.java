package com.sara.api.controller;

import com.sara.api.model.ConfiguracaoBoleto;
import com.sara.api.service.ConfiguracaoBoletoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/configuracoes-boleto")
@Tag(name = "Configurações de Boleto", description = "Endpoints para gerenciar regras de juros, multa e desconto de boleto")
@PreAuthorize("hasRole('ADMIN')")
public class ConfiguracaoBoletoController {

    @Autowired
    private ConfiguracaoBoletoService service;

    @GetMapping
    @Operation(summary = "Busca a configuração atual de boleto")
    public ResponseEntity<ConfiguracaoBoleto> get() {
        return ResponseEntity.ok(service.getConfiguracao());
    }

    @PutMapping
    @Operation(summary = "Atualiza as configurações de boleto")
    public ResponseEntity<ConfiguracaoBoleto> update(@RequestBody ConfiguracaoBoleto config) {
        return ResponseEntity.ok(service.salvar(config));
    }
}
