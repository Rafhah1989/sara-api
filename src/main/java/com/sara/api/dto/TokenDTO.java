package com.sara.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Token de autenticação gerado pelo sistema")
public record TokenDTO(
    @Schema(description = "Token JWT", example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...")
    String token
) {}
