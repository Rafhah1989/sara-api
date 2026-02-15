package com.sara.api.dto;

import com.sara.api.model.Role;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "Dados de retorno do usuário")
public class UsuarioResponseDTO {
    @Schema(description = "ID único do usuário no banco de dados", example = "1")
    private Long id;

    @Schema(description = "Nome completo do usuário", example = "João da Silva")
    private String nome;

    @Schema(description = "CEP formatado", example = "01001-000")
    private String cep;

    @Schema(description = "Endereço completo", example = "Praça da Sé")
    private String endereco;

    @Schema(description = "Número", example = "100")
    private String numero;

    @Schema(description = "Cidade", example = "São Paulo")
    private String cidade;

    @Schema(description = "UF", example = "SP")
    private String uf;

    @Schema(description = "CPF ou CNPJ formatado", example = "123.456.789-01")
    private String cpfCnpj;

    @Schema(description = "Telefone formatado", example = "(11) 98888-7777")
    private String telefone;

    @Schema(description = "Bairro", example = "Centro")
    private String bairro;

    @Schema(description = "Observação", example = "Observação teste")
    private String observacao;

    @Schema(description = "Padre", example = "Padre José")
    private String padre;

    @Schema(description = "Secretário", example = "João")
    private String secretario;

    @Schema(description = "Tesoureiro", example = "Maria")
    private String tesoureiro;

    @Schema(description = "Forma de Pagamento", example = "PIX")
    private String formaPagamento;

    @Schema(description = "Desconto", example = "5.0")
    private java.math.BigDecimal desconto;

    @Schema(description = "Modalidade de Entrega", example = "Retirada")
    private String modalidadeEntrega;

    @Schema(description = "ID do Setor", example = "1")
    private Long setorId;

    @Schema(description = "ID da Tabela de Frete", example = "1")
    private Long tabelaFreteId;

    @Schema(description = "Perfil de acesso", example = "ADMIN")
    private Role role;

    @Schema(description = "Indica se o usuário está ativo no sistema", example = "true")
    private Boolean ativo;
}
