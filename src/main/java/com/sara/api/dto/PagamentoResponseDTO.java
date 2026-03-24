package com.sara.api.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class PagamentoResponseDTO {
    private Long id;
    private Long formaPagamentoId;
    private String formaPagamentoDescricao;
    private LocalDate dataVencimento;
    private Boolean pago;
    private BigDecimal valor;
}
