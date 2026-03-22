package com.sara.api.dto;

import com.sara.api.model.Role;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "Resposta formatada do Carrinho contendo informações embutidas do Produto e Usuário")
public class CarrinhoResponseDTO {
    
    @Schema(description = "ID do Usuário")
    private Long usuarioId;
    @Schema(description = "Nome do Usuário")
    private String usuarioNome;
    @Schema(description = "CPF/CNPJ do Usuário")
    private String usuarioCpfCnpj;
    @Schema(description = "Role do Usuário")
    private Role usuarioRole;
    
    @Schema(description = "ID do Produto")
    private Long produtoId;
    @Schema(description = "Nome do Produto")
    private String produtoNome;
    @Schema(description = "Código do Produto")
    private String produtoCodigo;
    @Schema(description = "Preço do Produto")
    private Double produtoPreco;
    @Schema(description = "Indica se o produto possui imagem")
    private Boolean temImagem;
    @Schema(description = "Tamanho do Produto")
    private Integer produtoTamanho;
    @Schema(description = "Status Ativo do Produto")
    private Boolean produtoAtivo;

    @Schema(description = "Peso do Produto")
    private Double produtoPeso;

    @Schema(description = "Imagem do Produto (Base64)")
    private String produtoImagem;
    
    @Schema(description = "Quantidade no carrinho")
    private Integer quantidade;
}
