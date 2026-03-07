package com.sara.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "Dados para inserir/remover item do carrinho")
public class CarrinhoRequestDTO {
    @Schema(description = "ID do Usuário")
    private Long usuarioId;
    
    @Schema(description = "ID do Produto")
    private Long produtoId;

    @Schema(description = "Quantidade a adicionar ou atualizar (padrão 1)")
    private Integer quantidade = 1;
}
