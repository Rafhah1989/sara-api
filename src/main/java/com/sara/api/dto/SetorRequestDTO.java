package com.sara.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.util.List;

@Data
@Schema(description = "Dados para cadastro ou atualização de setor")
public class SetorRequestDTO {
    @Schema(description = "Nome descritivo do setor", example = "Logística")
    private String descricao;

    @Schema(description = "Indica se o setor deve ser criado como ativo", example = "true")
    private Boolean ativo;

    @Schema(description = "Lista de IDs das tabelas de frete vinculadas", example = "[1, 2]")
    private List<Long> tabelasFreteIds;
}
