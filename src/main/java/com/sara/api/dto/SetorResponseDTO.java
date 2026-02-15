package com.sara.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.util.List;

@Data
@Schema(description = "Dados de retorno do setor")
public class SetorResponseDTO {
    @Schema(description = "ID único do setor no banco de dados", example = "1")
    private Long id;

    @Schema(description = "Nome descritivo do setor", example = "Logística")
    private String descricao;

    @Schema(description = "Indica se o setor está ativo no sistema", example = "true")
    private Boolean ativo;

    @Schema(description = "Lista de tabelas de frete vinculadas a este setor")
    private List<TabelaFreteResponseDTO> tabelasFrete;
}
