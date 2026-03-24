package com.sara.api.service;

import com.sara.api.dto.FormaPagamentoDTO;
import com.sara.api.model.FormaPagamento;
import com.sara.api.repository.FormaPagamentoRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class FormaPagamentoService {

    @Autowired
    private FormaPagamentoRepository formaPagamentoRepository;

    public FormaPagamentoDTO criar(FormaPagamentoDTO dto) {
        FormaPagamento entidade = new FormaPagamento();
        BeanUtils.copyProperties(dto, entidade, "id");
        return toDTO(formaPagamentoRepository.save(entidade));
    }

    public FormaPagamentoDTO alterar(Long id, FormaPagamentoDTO dto) {
        FormaPagamento entidade = formaPagamentoRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Forma de Pagamento não encontrada"));
        
        BeanUtils.copyProperties(dto, entidade, "id");
        return toDTO(formaPagamentoRepository.save(entidade));
    }

    public void excluir(Long id) {
        if (!formaPagamentoRepository.existsById(id)) {
            throw new EntityNotFoundException("Forma de Pagamento não encontrada");
        }
        formaPagamentoRepository.deleteById(id);
    }

    public FormaPagamentoDTO buscarPorId(Long id) {
        return formaPagamentoRepository.findById(id)
                .map(this::toDTO)
                .orElseThrow(() -> new EntityNotFoundException("Forma de Pagamento não encontrada"));
    }

    public List<FormaPagamentoDTO> listarTodos() {
        return formaPagamentoRepository.findAll().stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    private FormaPagamentoDTO toDTO(FormaPagamento entidade) {
        FormaPagamentoDTO dto = new FormaPagamentoDTO();
        BeanUtils.copyProperties(entidade, dto);
        dto.setValorMinimo(entidade.getValorMinimo());
        return dto;
    }
}
