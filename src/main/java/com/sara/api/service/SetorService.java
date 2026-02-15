package com.sara.api.service;

import com.sara.api.dto.SetorRequestDTO;
import com.sara.api.dto.SetorResponseDTO;
import com.sara.api.dto.TabelaFreteResponseDTO;
import com.sara.api.exception.ValidationException;
import com.sara.api.model.Setor;
import com.sara.api.model.TabelaFrete;
import com.sara.api.repository.SetorRepository;
import com.sara.api.repository.TabelaFreteRepository;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class SetorService {

    @Autowired
    private SetorRepository repository;

    @Autowired
    private TabelaFreteRepository tabelaFreteRepository;

    public List<SetorResponseDTO> listarTodos() {
        return repository.findAll().stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    public List<SetorResponseDTO> buscarPorDescricao(String descricao) {
        return repository.findByDescricaoContainingIgnoreCase(descricao).stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    public Optional<SetorResponseDTO> buscarPorId(Long id) {
        return repository.findById(id).map(this::toDTO);
    }

    public SetorResponseDTO cadastrar(SetorRequestDTO request) {
        validar(request);
        Setor setor = new Setor();
        BeanUtils.copyProperties(request, setor, "tabelasFreteIds");
        if (setor.getAtivo() == null) {
            setor.setAtivo(true);
        }

        List<TabelaFrete> tabelas = request.getTabelasFreteIds().stream()
                .map(id -> tabelaFreteRepository.findById(id)
                        .orElseThrow(() -> new ValidationException("Tabela de frete não encontrada com id: " + id)))
                .collect(Collectors.toList());

        setor.setTabelasFrete(tabelas);
        return toDTO(repository.save(setor));
    }

    public SetorResponseDTO alterar(Long id, SetorRequestDTO request) {
        validar(request);
        return repository.findById(id).map(setor -> {
            BeanUtils.copyProperties(request, setor, "id", "tabelasFreteIds");

            List<TabelaFrete> tabelas = request.getTabelasFreteIds().stream()
                    .map(fid -> tabelaFreteRepository.findById(fid)
                            .orElseThrow(
                                    () -> new ValidationException("Tabela de frete não encontrada com id: " + fid)))
                    .collect(Collectors.toList());

            setor.setTabelasFrete(tabelas);
            return toDTO(repository.save(setor));
        }).orElseThrow(() -> new ValidationException("Setor não encontrado com id: " + id));
    }

    public void desativar(Long id) {
        repository.findById(id).ifPresentOrElse(setor -> {
            setor.setAtivo(false);
            repository.save(setor);
        }, () -> {
            throw new ValidationException("Setor não encontrado com id: " + id);
        });
    }

    public void ativar(Long id) {
        repository.findById(id).ifPresentOrElse(setor -> {
            setor.setAtivo(true);
            repository.save(setor);
        }, () -> {
            throw new ValidationException("Setor não encontrado com id: " + id);
        });
    }

    private void validar(SetorRequestDTO request) {
        if (request.getDescricao() == null || request.getDescricao().trim().isEmpty()) {
            throw new ValidationException("A descrição do setor é obrigatória");
        }
        if (request.getAtivo() == null) {
            throw new ValidationException("O campo ativo é obrigatório");
        }
    }

    private SetorResponseDTO toDTO(Setor setor) {
        SetorResponseDTO dto = new SetorResponseDTO();
        BeanUtils.copyProperties(setor, dto, "tabelasFrete");

        if (setor.getTabelasFrete() != null) {
            List<TabelaFreteResponseDTO> tabelasDTO = setor.getTabelasFrete().stream()
                    .map(t -> {
                        TabelaFreteResponseDTO tdto = new TabelaFreteResponseDTO();
                        BeanUtils.copyProperties(t, tdto);
                        return tdto;
                    }).collect(Collectors.toList());
            dto.setTabelasFrete(tabelasDTO);
        }

        return dto;
    }
}
