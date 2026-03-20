package com.sara.api.dto;

import com.sara.api.model.Produto;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class ProdutoMiniDTO {
    private Long id;
    private String nome;
    private Integer tamanho;
    private Boolean ativo;
    private Long codigo;
    private Double preco;
    private Double peso;
    private Boolean temImagem;

    public ProdutoMiniDTO(Long id, String nome, Integer tamanho, Boolean ativo, Long codigo, Double preco, Double peso, Boolean temImagem) {
        this.id = id;
        this.nome = nome;
        this.tamanho = tamanho;
        this.ativo = ativo;
        this.codigo = codigo;
        this.preco = preco;
        this.peso = peso;
        this.temImagem = temImagem;
    }

    public ProdutoMiniDTO(Produto produto) {
        this.id = produto.getId();
        this.nome = produto.getNome();
        this.tamanho = produto.getTamanho();
        this.ativo = produto.getAtivo();
        this.codigo = produto.getCodigo();
        this.preco = produto.getPreco();
        this.peso = produto.getPeso();
        this.temImagem = produto.getImagem() != null && !produto.getImagem().isEmpty();
    }
}
