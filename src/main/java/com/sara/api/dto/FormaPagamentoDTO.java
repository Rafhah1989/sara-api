package com.sara.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "Dados para transferência de Forma de Pagamento")
public class FormaPagamentoDTO {

    @Schema(description = "ID único da Forma de Pagamento", example = "1")
    private Long id;

    @Schema(description = "Descrição ou nome", example = "PIX")
    private String descricao;

    @Schema(description = "Percentual de desconto atrelado a essa modalidade", example = "5.0")
    private Double desconto;

    @Schema(description = "Valor mínimo total para disponibilizar esta forma (à vista)", example = "50.0")
    private java.math.BigDecimal valorMinimo = java.math.BigDecimal.ZERO;

    @Schema(description = "Ícone de sistema utilizado na renderização", example = "fa-brands fa-pix")
    private String iconeFontAwesome;
}
