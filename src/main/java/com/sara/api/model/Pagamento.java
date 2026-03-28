package com.sara.api.model;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;

@Entity
@Getter
@Setter
@ToString(onlyExplicitlyIncluded = true)
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@NoArgsConstructor
@AllArgsConstructor
public class Pagamento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    @ToString.Include
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pedido_id")
    private Pedido pedido;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "forma_pagamento_id", nullable = false)
    private FormaPagamento formaPagamento;

    @ToString.Include
    private LocalDate dataVencimento;

    @Column(nullable = false)
    private Boolean pago = false;

    @Column(nullable = false)
    private BigDecimal valor;

    @Column(name = "pix_copia_e_cola", columnDefinition = "TEXT")
    private String pixCopiaECola;

    @Column(name = "pix_qr_code", columnDefinition = "TEXT")
    private String pixQrCode;

    @Column(name = "mercadopago_pagamento_id", length = 100)
    private String mercadopagoPagamentoId;

    @Column(name = "pagamento_online")
    private Boolean pagamentoOnline = false;

    @Column(name = "data_expiracao_pix")
    private OffsetDateTime dataExpiracaoPix;
}
