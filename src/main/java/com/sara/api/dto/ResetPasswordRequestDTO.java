package com.sara.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Dados para redefinir a senha")
public record ResetPasswordRequestDTO(
    @Schema(description = "Token de recuperação recebido por e-mail")
    String token,
    
    @Schema(description = "Nova senha (6 dígitos numéricos)", example = "654321")
    String novaSenha
) {}
