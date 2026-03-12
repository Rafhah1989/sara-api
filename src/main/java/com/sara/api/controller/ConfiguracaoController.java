package com.sara.api.controller;

import com.sara.api.dto.ConfiguracaoRequestDTO;
import com.sara.api.dto.ConfiguracaoResponseDTO;
import com.sara.api.model.Configuracao;
import com.sara.api.repository.ConfiguracaoRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/configuracao")
@RequiredArgsConstructor
@Tag(name = "Configuração", description = "Gerenciamento das configurações do sistema (e-mail, etc)")
public class ConfiguracaoController {

    private final ConfiguracaoRepository repository;

    @GetMapping
    @Operation(summary = "Busca a configuração do sistema", description = "Retorna a primeira configuração encontrada no banco")
    public ResponseEntity<ConfiguracaoResponseDTO> buscar() {
        return repository.findAll().stream().findFirst()
                .map(this::convertToResponseDTO)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    @Operation(summary = "Cria ou atualiza a configuração", description = "Se já existir uma configuração, ela será atualizada")
    public ResponseEntity<ConfiguracaoResponseDTO> salvar(@RequestBody ConfiguracaoRequestDTO request) {
        Configuracao config = repository.findAll().stream().findFirst().orElse(new Configuracao());
        updateEntityFromDTO(config, request);
        Configuracao saved = repository.save(config);
        return ResponseEntity.ok(convertToResponseDTO(saved));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualiza a configuração por ID")
    public ResponseEntity<ConfiguracaoResponseDTO> atualizar(@PathVariable Long id, @RequestBody ConfiguracaoRequestDTO request) {
        return repository.findById(id)
                .map(config -> {
                    updateEntityFromDTO(config, request);
                    return repository.save(config);
                })
                .map(this::convertToResponseDTO)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    private void updateEntityFromDTO(Configuracao entity, ConfiguracaoRequestDTO dto) {
        entity.setMailHost(dto.getMailHost());
        entity.setMailPort(dto.getMailPort());
        entity.setMailUsername(dto.getMailUsername());
        
        // Se a senha vier preenchida, atualiza. Se vier vazia, mantém a anterior.
        if (dto.getMailPassword() != null && !dto.getMailPassword().trim().isEmpty()) {
            entity.setMailPassword(dto.getMailPassword());
        }
        
        entity.setMailAuth(dto.getMailAuth());
        entity.setMailStarttls(dto.getMailStarttls());
        entity.setEmailsNotificacao(dto.getEmailsNotificacao());
        entity.setEmailAtivo(dto.getEmailAtivo());
    }

    private ConfiguracaoResponseDTO convertToResponseDTO(Configuracao entity) {
        ConfiguracaoResponseDTO dto = new ConfiguracaoResponseDTO();
        dto.setId(entity.getId());
        dto.setMailHost(entity.getMailHost());
        dto.setMailPort(entity.getMailPort());
        dto.setMailUsername(entity.getMailUsername());
        dto.setMailPassword(entity.getMailPassword());
        dto.setMailAuth(entity.getMailAuth());
        dto.setMailStarttls(entity.getMailStarttls());
        dto.setEmailsNotificacao(entity.getEmailsNotificacao());
        dto.setEmailAtivo(entity.getEmailAtivo());
        return dto;
    }
}
