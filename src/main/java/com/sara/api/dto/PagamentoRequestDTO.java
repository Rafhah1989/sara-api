package com.sara.api.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class PagamentoRequestDTO {
    private Long id;
    private Long formaPagamentoId;
    private LocalDate dataVencimento;
    private Boolean pago;
    private BigDecimal valor;
}
