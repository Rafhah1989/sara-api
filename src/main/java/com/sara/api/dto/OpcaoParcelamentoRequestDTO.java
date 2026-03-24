package com.sara.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class OpcaoParcelamentoRequestDTO {
    private Long formaPagamentoId;
    private Integer qtdMaxParcelas;
    private Integer diasVencimentoIntervalo;
    @JsonProperty("valorMinimoParcela")
    private java.math.BigDecimal valorMinimoParcela;
}
