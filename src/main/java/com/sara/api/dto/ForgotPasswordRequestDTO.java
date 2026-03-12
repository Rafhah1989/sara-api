package com.sara.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Dados para solicitar recuperação de senha")
public record ForgotPasswordRequestDTO(
    @Schema(description = "CPF ou CNPJ do usuário", example = "12345678901")
    String cpfCnpj
) {}
