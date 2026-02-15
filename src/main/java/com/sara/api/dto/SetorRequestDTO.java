package com.sara.api.dto;

import lombok.Data;
import java.util.List;

@Data
public class SetorRequestDTO {
    private String descricao;
    private Boolean ativo;
    private List<Long> tabelasFreteIds;
}
