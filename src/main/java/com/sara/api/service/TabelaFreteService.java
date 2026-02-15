package com.sara.api.service;

import com.sara.api.dto.TabelaFreteRequestDTO;
import com.sara.api.dto.TabelaFreteResponseDTO;
import com.sara.api.model.TabelaFrete;
import com.sara.api.repository.TabelaFreteRepository;
import com.sara.api.exception.ValidationException;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class TabelaFreteService {

    @Autowired
    private TabelaFreteRepository repository;

    public List<TabelaFreteResponseDTO> listarTodas() {
        return repository.findAll().stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    public List<TabelaFreteResponseDTO> buscarPorDescricao(String descricao) {
        return repository.findByDescricaoContainingIgnoreCase(descricao).stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    public Optional<TabelaFreteResponseDTO> buscarPorId(Long id) {
        return repository.findById(id).map(this::toDTO);
    }

    public TabelaFreteResponseDTO cadastrar(TabelaFreteRequestDTO request) {
        validar(request);
        TabelaFrete tabela = new TabelaFrete();
        BeanUtils.copyProperties(request, tabela);
        if (tabela.getAtivo() == null) {
            tabela.setAtivo(true);
        }
        return toDTO(repository.save(tabela));
    }

    public TabelaFreteResponseDTO alterar(Long id, TabelaFreteRequestDTO request) {
        validar(request);
        return repository.findById(id).map(tabela -> {
            BeanUtils.copyProperties(request, tabela, "id");
            return toDTO(repository.save(tabela));
        }).orElseThrow(() -> new ValidationException("Tabela de frete não encontrada com id: " + id));
    }

    public void desativar(Long id) {
        repository.findById(id).ifPresentOrElse(tabela -> {
            tabela.setAtivo(false);
            repository.save(tabela);
        }, () -> {
            throw new ValidationException("Tabela de frete não encontrada com id: " + id);
        });
    }

    private void validar(TabelaFreteRequestDTO request) {
        if (request.getDescricao() == null || request.getDescricao().trim().isEmpty()) {
            throw new ValidationException("A descrição da tabela de frete é obrigatória");
        }
        if (request.getValor() == null) {
            throw new ValidationException("O valor da tabela de frete é obrigatório");
        }
        if (request.getAtivo() == null) {
            // Se for nulo no DTO, o banco será preenchido pelo padrão ou pelo service,
            // mas o requisito disse "ativo (Boolean, obrigatório)".
            // No entanto, geralmente no cadastro se não vier, assumimos true.
            // Para ser formal com o "obrigatório", vamos validar:
            throw new ValidationException("O campo ativo é obrigatório");
        }
    }

    private TabelaFreteResponseDTO toDTO(TabelaFrete tabela) {
        TabelaFreteResponseDTO dto = new TabelaFreteResponseDTO();
        BeanUtils.copyProperties(tabela, dto);
        return dto;
    }
}
