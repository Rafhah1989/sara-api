package com.sara.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.math.BigDecimal;

@Data
@Schema(description = "Dados de retorno da tabela de frete")
public class TabelaFreteResponseDTO {
    @Schema(description = "ID único da tabela de frete no banco de dados", example = "1")
    private Long id;

    @Schema(description = "Descrição da rota ou tipo de frete", example = "São Paulo - Capital")
    private String descricao;

    @Schema(description = "Valor do frete", example = "50.00")
    private BigDecimal valor;

    @Schema(description = "Indica se a tabela está ativa no sistema", example = "true")
    private Boolean ativo;
}
