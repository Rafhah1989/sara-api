package com.sara.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.math.BigDecimal;

@Data
@Schema(description = "Objeto de requisição para um item de produto dentro de um pedido")
public class PedidoProdutoRequestDTO {

    @Schema(description = "ID do produto", example = "1")
    private Long produtoId;

    @Schema(description = "Valor unitário do produto no momento da venda", example = "50.00")
    private BigDecimal valor;

    @Schema(description = "Quantidade do produto no pedido", example = "2.0")
    private BigDecimal quantidade;

    @Schema(description = "Desconto unitário do produto", example = "5.00")
    private BigDecimal desconto;

    @Schema(description = "Peso do produto", example = "1.50")
    private BigDecimal peso;
}
