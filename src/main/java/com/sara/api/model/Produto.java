package com.sara.api.model;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Modelo que representa um produto no catálogo")
public class Produto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nome;
    private Integer tamanho;
    private Boolean ativo = true;
    @Column(columnDefinition = "TEXT")
    @Basic(fetch = FetchType.LAZY)
    private String imagem;
    private Long codigo;
    private Double preco;
    private Double peso;
    private Boolean temImagem = false;

    @PrePersist
    @PreUpdate
    public void atualizarTemImagem() {
        this.temImagem = this.imagem != null && !this.imagem.isEmpty();
    }
}
