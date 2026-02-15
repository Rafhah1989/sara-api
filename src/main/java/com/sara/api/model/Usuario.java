package com.sara.api.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String nome;

    @Column(nullable = false, length = 8)
    private String cep;

    private String endereco;

    private String numero;

    @Column(nullable = false, length = 100)
    private String cidade;

    @Column(nullable = false, length = 2)
    private String uf;

    @Column(nullable = false, length = 14)
    private String cpfCnpj;

    @Column(length = 15)
    private String telefone;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    @Column(nullable = false, length = 100)
    private String bairro = "A designar";

    private String observacao;

    private String padre;

    private String secretario;

    private String tesoureiro;

    @Enumerated(EnumType.STRING)
    @Column(name = "forma_pagamento")
    private FormaPagamento formaPagamento;

    private java.math.BigDecimal desconto;

    @Column(name = "modalidade_entrega")
    private String modalidadeEntrega;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "setor_id")
    private Setor setor;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tabela_frete_id")
    private TabelaFrete tabelaFrete;

    @Column(nullable = false, length = 32)
    private String senha;

    private Boolean ativo = true;
}
