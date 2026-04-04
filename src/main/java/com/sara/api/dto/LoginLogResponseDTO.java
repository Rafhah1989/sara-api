package com.sara.api.dto;

import com.sara.api.model.LoginStatus;
import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Builder
public class LoginLogResponseDTO {
    private Long id;
    private Long usuarioId;
    private String usuarioNome;
    private String usuarioCpfCnpj;
    private String usuarioRole;
    private LocalDateTime dataHora;
    private String userAgent;
    private LoginStatus status;
    private String local;
}
