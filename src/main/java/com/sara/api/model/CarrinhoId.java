package com.sara.api.model;

import jakarta.persistence.Embeddable;
import java.io.Serializable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Embeddable
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CarrinhoId implements Serializable {
    private Long usuarioId;
    private Long produtoId;
}
