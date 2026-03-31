package com.sara.api.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;

@Data
public class PagamentoResponseDTO {
    private Long id;
    private Long formaPagamentoId;
    private String formaPagamentoDescricao;
    private LocalDate dataVencimento;
    private Boolean pago;
    private BigDecimal valor;
    private String pixCopiaECola;
    private String pixQrCode;
    private String boletoPdfUrl;
    private String boletoLinhaDigitavel;
    private String boletoCodigoBarras;
    private String mercadopagoPagamentoId;
    private Boolean pagamentoOnline;
    private OffsetDateTime dataExpiracao;
}
