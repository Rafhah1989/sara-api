package com.sara.api.service;

import com.sara.api.dto.LoginLogResponseDTO;
import com.sara.api.model.LoginLog;
import com.sara.api.repository.LoginLogRepository;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class LoginLogService {

    private final LoginLogRepository loginLogRepository;

    @Transactional(readOnly = true)
    public Page<LoginLogResponseDTO> findAll(
            String usuarioNome,
            String usuarioRole,
            LocalDateTime dataInicio,
            LocalDateTime dataFim,
            Pageable pageable) {

        Specification<LoginLog> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (usuarioNome != null && !usuarioNome.isEmpty()) {
                predicates.add(cb.like(cb.upper(root.get("usuario").get("nome")), "%" + usuarioNome.toUpperCase() + "%"));
            }

            if (usuarioRole != null && !usuarioRole.isEmpty()) {
                try {
                    com.sara.api.model.Role roleEnum = com.sara.api.model.Role.valueOf(usuarioRole.toUpperCase());
                    predicates.add(cb.equal(root.get("usuario").get("role"), roleEnum));
                } catch (IllegalArgumentException e) {
                    // Ignora filtro inválido
                }
            }

            if (dataInicio != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("dataHora"), dataInicio));
            }

            if (dataFim != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("dataHora"), dataFim));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };

        return loginLogRepository.findAll(spec, pageable).map(this::convertToDTO);
    }

    private LoginLogResponseDTO convertToDTO(LoginLog log) {
        return LoginLogResponseDTO.builder()
                .id(log.getId())
                .usuarioId(log.getUsuario() != null ? log.getUsuario().getId() : null)
                .usuarioNome(log.getUsuario() != null ? log.getUsuario().getNome() : "Desconhecido")
                .usuarioCpfCnpj(log.getUsuario() != null ? log.getUsuario().getCpfCnpj() : null)
                .usuarioRole(log.getUsuario() != null && log.getUsuario().getRole() != null ? log.getUsuario().getRole().name() : null)
                .dataHora(log.getDataHora())
                .userAgent(log.getUserAgent())
                .status(log.getStatus())
                .build();
    }
}
