package com.sara.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.math.BigDecimal;
import java.util.List;

@Data
@Schema(description = "Objeto de requisição para incluir ou alterar um pedido")
public class PedidoRequestDTO {

    @Schema(description = "ID do usuário que realizou o pedido", example = "1")
    private Long usuarioId;

    @Schema(description = "Valor do desconto aplicado ao pedido", example = "10.00")
    private BigDecimal desconto;

    @Schema(description = "Valor do frete do pedido", example = "15.00")
    private BigDecimal frete;

    @Schema(description = "Valor total do pedido (obrigatório)", example = "150.00")
    private BigDecimal valorTotal;

    @Schema(description = "Observação opcional sobre o pedido", example = "Entregar após as 14h")
    private String observacao;

    @Schema(description = "Lista de produtos e seus detalhes no pedido")
    private List<PedidoProdutoRequestDTO> produtos;
}
