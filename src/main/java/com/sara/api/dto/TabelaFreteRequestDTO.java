package com.sara.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.math.BigDecimal;

@Data
@Schema(description = "Dados para cadastro ou atualização de tabela de frete")
public class TabelaFreteRequestDTO {
    @Schema(description = "Descrição da rota ou tipo de frete", example = "São Paulo - Capital")
    private String descricao;

    @Schema(description = "Valor do frete", example = "50.00")
    private BigDecimal valor;

    @Schema(description = "Indica se a tabela deve ser criada como ativa", example = "true")
    private Boolean ativo;
}
