package com.sara.api.dto;

import lombok.Data;

@Data
public class ConfirmacaoPedidoRequestDTO {
    private boolean enviarEmail;
    private boolean ajustarDatas;
}
