package com.sara.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.math.BigDecimal;
import java.util.List;

@Data
@Schema(description = "Resposta contendo os detalhes de um pedido")
public class PedidoResponseDTO {

    @Schema(description = "ID do pedido", example = "1")
    private Long id;

    @Schema(description = "ID do usuário do pedido", example = "1")
    private Long usuarioId;

    @Schema(description = "Nome do usuário (opcional para exibição)", example = "João da Silva")
    private String usuarioNome;

    @Schema(description = "Desconto total do pedido", example = "10.00")
    private BigDecimal desconto;

    @Schema(description = "Valor do frete", example = "15.00")
    private BigDecimal frete;

    @Schema(description = "Valor total final", example = "150.00")
    private BigDecimal valorTotal;

    @Schema(description = "Observação", example = "Entregue")
    private String observacao;

    @Schema(description = "Status de cancelamento", example = "false")
    private Boolean cancelado;

    @Schema(description = "Lista detalhada dos produtos no pedido")
    private List<PedidoProdutoResponseDTO> produtos;
}
