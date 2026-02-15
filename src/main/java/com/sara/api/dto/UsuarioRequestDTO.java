package com.sara.api.dto;

import com.sara.api.model.Role;
import com.sara.api.model.FormaPagamento;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "Dados para criação ou atualização de usuário")
public class UsuarioRequestDTO {
    @Schema(description = "Nome completo do usuário", example = "João da Silva")
    private String nome;

    @Schema(description = "CEP do endereço (apenas números)", example = "01001000")
    private String cep;

    @Schema(description = "Endereço completo (Rua, Av, etc)", example = "Praça da Sé")
    private String endereco;

    @Schema(description = "Número da residência", example = "100")
    private String numero;

    @Schema(description = "Cidade de residência", example = "São Paulo")
    private String cidade;

    @Schema(description = "Unidade Federativa", example = "SP")
    private String uf;

    @Schema(description = "CPF ou CNPJ do usuário", example = "12345678901")
    private String cpfCnpj;

    @Schema(description = "Telefone de contato", example = "(11) 98888-7777")
    private String telefone;

    @Schema(description = "Bairro", example = "Centro")
    private String bairro;

    @Schema(description = "Observação adicional", example = "Cliente preferencial")
    private String observacao;

    @Schema(description = "Nome do Padre", example = "Padre Marcelo Rossi")
    private String padre;

    @Schema(description = "Nome do Secretário", example = "Marcos")
    private String secretario;

    @Schema(description = "Nome do Tesoureiro", example = "Paula")
    private String tesoureiro;

    @Schema(description = "Forma de pagamento preferencial", example = "PIX")
    private FormaPagamento formaPagamento;

    @Schema(description = "Percentual de desconto", example = "10.0")
    private java.math.BigDecimal desconto;

    @Schema(description = "Modalidade de entrega", example = "Transportadora")
    private String modalidadeEntrega;

    @Schema(description = "ID do Setor", example = "1")
    private Long setorId;

    @Schema(description = "ID da Tabela de Frete", example = "1")
    private Long tabelaFreteId;

    @Schema(description = "Perfil de acesso do usuário", example = "ADMIN")
    private Role role;

    @Schema(description = "Senha de acesso (6 dígitos numéricos)", example = "123456")
    private String senha;
}
