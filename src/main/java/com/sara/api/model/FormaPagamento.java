package com.sara.api.model;

import lombok.Getter;

@Getter
public enum FormaPagamento {
    PIX("PIX"),
    DINHEIRO("Dinheiro");

    private final String descricao;

    FormaPagamento(String descricao) {
        this.descricao = descricao;
    }
}
