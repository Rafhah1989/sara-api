package com.sara.api.dto;

import lombok.Data;
import java.util.List;

@Data
public class SetorResponseDTO {
    private Long id;
    private String descricao;
    private Boolean ativo;
    private List<TabelaFreteResponseDTO> tabelasFrete;
}
