package com.sara.api.dto;

import com.sara.api.model.Produto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProdutoResumoDTO {
    private Long id;
    private String nome;
    private Integer tamanho;
    private Boolean ativo;
    private String codigo;
    private Double preco;
    private Double peso;
    private Boolean temImagem;
    private String imagem;

    public ProdutoResumoDTO(Produto produto) {
        this.id = produto.getId();
        this.nome = produto.getNome();
        this.tamanho = produto.getTamanho();
        this.ativo = produto.getAtivo();
        this.codigo = produto.getCodigo();
        this.preco = produto.getPreco();
        this.peso = produto.getPeso();
        this.temImagem = produto.getImagem() != null && !produto.getImagem().isEmpty();
        this.imagem = produto.getImagem();
    }
}
