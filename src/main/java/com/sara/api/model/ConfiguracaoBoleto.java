package com.sara.api.model;

import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;

@Data
@Entity
@Table(name = "configuracoes_boleto")
public class ConfiguracaoBoleto {

    @Id
    private Long id = 1L; // Singleton pattern

    @Column(name = "multa_tipo", length = 20, nullable = false)
    private String multaTipo = "percentage"; // 'fixed' ou 'percentage'

    @Column(name = "multa_valor", nullable = false)
    private BigDecimal multaValor = BigDecimal.ZERO;

    @Column(name = "juros_tipo", length = 20, nullable = false)
    private String jurosTipo = "percentage";

    @Column(name = "juros_valor", nullable = false)
    private BigDecimal jurosValor = BigDecimal.ZERO;

    @Column(name = "desconto_tipo", length = 20, nullable = false)
    private String descontoTipo = "percentage";

    @Column(name = "desconto_valor", nullable = false)
    private BigDecimal descontoValor = BigDecimal.ZERO;

    @Column(name = "desconto_dias_antecedencia", nullable = false)
    private Integer descontoDiasAntecedencia = 0;
}
