package com.sara.api.dto;

import com.sara.api.model.SituacaoPedido;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Schema(description = "DTO simplificado para listagem de pedidos")
public class PedidoListResponseDTO {
    private Long id;
    private Long usuarioId;
    private String usuarioNome;
    private BigDecimal valorTotal;
    private SituacaoPedido situacao;
    private String situacaoDescricao;
    private LocalDateTime dataPedido;
    private Boolean pago;
    private Boolean cancelado;
    private Boolean pagamentoOnline;
    private String notaFiscalPath;
}
