package com.sara.api.service;

import com.sara.api.dto.OpcaoParcelamentoRequestDTO;
import com.sara.api.dto.OpcaoParcelamentoResponseDTO;
import com.sara.api.model.FormaPagamento;
import com.sara.api.model.OpcaoParcelamento;
import com.sara.api.repository.FormaPagamentoRepository;
import com.sara.api.repository.OpcaoParcelamentoRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OpcaoParcelamentoService {

    private final OpcaoParcelamentoRepository repository;
    private final FormaPagamentoRepository formaPagamentoRepository;

    public List<OpcaoParcelamentoResponseDTO> findAll() {
        return repository.findAll().stream()
                .map(this::convertToResponseDTO)
                .collect(Collectors.toList());
    }

    public List<OpcaoParcelamentoResponseDTO> findByFormaPagamento(Long formaPagamentoId) {
        // Simple filter in stream for now, or could add repository method
        return repository.findAll().stream()
                .filter(o -> o.getFormaPagamento().getId().equals(formaPagamentoId))
                .map(this::convertToResponseDTO)
                .collect(Collectors.toList());
    }

    public OpcaoParcelamentoResponseDTO findById(Long id) {
        OpcaoParcelamento entity = repository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Opção de parcelamento não encontrada: " + id));
        return convertToResponseDTO(entity);
    }

    @Transactional
    public OpcaoParcelamentoResponseDTO create(OpcaoParcelamentoRequestDTO request) {
        OpcaoParcelamento entity = new OpcaoParcelamento();
        updateEntityFromDTO(entity, request);
        OpcaoParcelamento saved = repository.save(entity);
        return convertToResponseDTO(saved);
    }

    @Transactional
    public OpcaoParcelamentoResponseDTO update(Long id, OpcaoParcelamentoRequestDTO request) {
        OpcaoParcelamento entity = repository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Opção de parcelamento não encontrada: " + id));
        updateEntityFromDTO(entity, request);
        OpcaoParcelamento saved = repository.save(entity);
        return convertToResponseDTO(saved);
    }

    @Transactional
    public void delete(Long id) {
        if (!repository.existsById(id)) {
            throw new EntityNotFoundException("Opção de parcelamento não encontrada: " + id);
        }
        // Primeiro remove as associações com usuários para evitar erro de integridade
        repository.deleteUserAssociations(id);
        repository.deleteById(id);
    }

    private void updateEntityFromDTO(OpcaoParcelamento entity, OpcaoParcelamentoRequestDTO dto) {
        if (dto.getFormaPagamentoId() != null) {
            FormaPagamento fp = formaPagamentoRepository.findById(dto.getFormaPagamentoId())
                    .orElseThrow(() -> new EntityNotFoundException("Forma de pagamento não encontrada: " + dto.getFormaPagamentoId()));
            entity.setFormaPagamento(fp);
        }
        entity.setQtdMaxParcelas(dto.getQtdMaxParcelas());
        entity.setDiasVencimentoIntervalo(dto.getDiasVencimentoIntervalo() != null ? dto.getDiasVencimentoIntervalo() : 30);
        entity.setValorMinimoParcela(dto.getValorMinimoParcela() != null ? dto.getValorMinimoParcela() : java.math.BigDecimal.ZERO);
    }

    private OpcaoParcelamentoResponseDTO convertToResponseDTO(OpcaoParcelamento entity) {
        OpcaoParcelamentoResponseDTO dto = new OpcaoParcelamentoResponseDTO();
        dto.setId(entity.getId());
        dto.setFormaPagamentoId(entity.getFormaPagamento().getId());
        dto.setFormaPagamentoDescricao(entity.getFormaPagamento().getDescricao());
        dto.setQtdMaxParcelas(entity.getQtdMaxParcelas());
        dto.setDiasVencimentoIntervalo(entity.getDiasVencimentoIntervalo());
        dto.setValorMinimoParcela(entity.getValorMinimoParcela());
        return dto;
    }
}
