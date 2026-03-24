package com.sara.api.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class OpcaoParcelamento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "forma_pagamento_id", nullable = false)
    private FormaPagamento formaPagamento;

    @Column(nullable = false)
    private Integer qtdMaxParcelas;

    @Column(nullable = false)
    private Integer diasVencimentoIntervalo = 30;

    @Column(name = "valor_minimo_parcela", precision = 19, scale = 2)
    private java.math.BigDecimal valorMinimoParcela = java.math.BigDecimal.ZERO;
}
