package com.sara.api.model;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@ToString(onlyExplicitlyIncluded = true)
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@NoArgsConstructor
@AllArgsConstructor
public class Pedido {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    @ToString.Include
    private Long id;

    @ManyToOne
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "forma_pagamento_id")
    private FormaPagamento formaPagamento;

    private BigDecimal desconto;
    private BigDecimal frete;

    @Column(nullable = false)
    private BigDecimal valorTotal;

    private String observacao;

    @Column(nullable = false)
    private Boolean cancelado = false;

    @Column(name = "data_pedido", updatable = false)
    @ToString.Include
    private LocalDateTime dataPedido;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @ToString.Include
    private SituacaoPedido situacao = SituacaoPedido.PENDENTE;
    
    private Boolean pago = false;

    @Column(name = "nota_fiscal_path")
    private String notaFiscalPath;
    
    @Column(name = "numero_nota_fiscal")
    private String numeroNotaFiscal;
    
    @Column(name = "data_faturamento")
    private LocalDate dataFaturamento;

    @OneToMany(mappedBy = "pedido", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Pagamento> pagamentos = new ArrayList<>();

    @OneToMany(mappedBy = "pedido", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PedidoProduto> produtos = new ArrayList<>();

    @PrePersist
    protected void onCreate() {
        dataPedido = LocalDateTime.now(ZoneId.of("America/Sao_Paulo"));
    }
}
