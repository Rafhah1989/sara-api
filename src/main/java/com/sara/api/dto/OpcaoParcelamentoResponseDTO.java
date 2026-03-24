package com.sara.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class OpcaoParcelamentoResponseDTO {
    private Long id;
    private Long formaPagamentoId;
    private String formaPagamentoDescricao;
    private Integer qtdMaxParcelas;
    private Integer diasVencimentoIntervalo;
    @JsonProperty("valorMinimoParcela")
    private java.math.BigDecimal valorMinimoParcela;
}
