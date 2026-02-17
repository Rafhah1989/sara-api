package com.sara.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.math.BigDecimal;

@Data
@Schema(description = "Resposta contendo detalhes de um produto dentro de um pedido")
public class PedidoProdutoResponseDTO {

    @Schema(description = "ID do registro de relacionamento", example = "1")
    private Long id;

    @Schema(description = "ID do produto", example = "1")
    private Long produtoId;

    @Schema(description = "Nome do produto", example = "Imagem de Resina Sagrada Família")
    private String produtoNome;

    @Schema(description = "Código do produto", example = "1001")
    private Long produtoCodigo;

    @Schema(description = "Valor histórico no momento da venda", example = "50.00")
    private BigDecimal valor;

    @Schema(description = "Quantidade do produto", example = "2.0")
    private BigDecimal quantidade;

    @Schema(description = "Desconto no item", example = "5.00")
    private BigDecimal desconto;

    @Schema(description = "Peso no item", example = "1.50")
    private BigDecimal peso;
}
