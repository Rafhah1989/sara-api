package com.sara.api.dto;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class TabelaFreteResponseDTO {
    private Long id;
    private String descricao;
    private BigDecimal valor;
    private Boolean ativo;
}
