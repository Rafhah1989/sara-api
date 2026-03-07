package com.sara.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Dados para autenticação do usuário")
public record LoginDTO(
    @Schema(description = "CPF ou CNPJ do usuário", example = "12345678901")
    String cpfCnpj,
    
    @Schema(description = "Senha de acesso (6 dígitos numéricos)", example = "123456")
    String senha
) {}
