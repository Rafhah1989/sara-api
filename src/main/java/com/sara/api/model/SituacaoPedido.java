package com.sara.api.model;

import lombok.Getter;

@Getter
public enum SituacaoPedido {
    PENDENTE("Pendente"),
    EM_PRODUCAO("Em Produção"),
    EM_ENTREGA("Em Entrega"),
    FINALIZADO("Finalizado");

    private final String descricao;

    SituacaoPedido(String descricao) {
        this.descricao = descricao;
    }
}
