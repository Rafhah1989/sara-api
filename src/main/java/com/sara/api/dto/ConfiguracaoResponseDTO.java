package com.sara.api.dto;

import lombok.Data;

@Data
public class ConfiguracaoResponseDTO {
    private Long id;
    private String mailHost;
    private Integer mailPort;
    private String mailUsername;
    private String mailPassword;
    private Boolean mailAuth;
    private Boolean mailStarttls;
    private String emailsNotificacao;
    private Boolean emailAtivo;
}
