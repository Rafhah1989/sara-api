package com.sara.api.model;

import lombok.Getter;

@Getter
public enum MetodoPagamentoAutorizado {
    APENAS_NA_ENTREGA("Apenas na entrega"),
    ENTREGA_E_ONLINE("Na entrega ou online"),
    APENAS_ONLINE("Apenas online");

    private final String descricao;

    MetodoPagamentoAutorizado(String descricao) {
        this.descricao = descricao;
    }
}
