package com.sara.api.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "carrinho")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Carrinho {

    @EmbeddedId
    private CarrinhoId id = new CarrinhoId();

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("usuarioId")
    @JoinColumn(name = "id_usuario")
    private Usuario usuario;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("produtoId")
    @JoinColumn(name = "id_produto")
    private Produto produto;

    private Integer quantidade;
}
