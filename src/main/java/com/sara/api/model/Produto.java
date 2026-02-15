package com.sara.api.model;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Column;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Modelo que representa um produto no catálogo")
public class Produto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(description = "ID único do produto", example = "1")
    private Long id;

    @Column(nullable = false)
    @Schema(description = "Nome do produto", example = "Imagem de Resina Sagrada Família")
    private String nome;

    @Schema(description = "Tamanho do produto em centímetros", example = "20")
    private Integer tamanho;

    @Schema(description = "Indica se o produto está ativo e visível no catálogo", example = "true")
    private Boolean ativo = true;

    @Column(columnDefinition = "TEXT")
    @Schema(description = "Representação em Base64 da imagem do produto")
    private String imagem;

}
