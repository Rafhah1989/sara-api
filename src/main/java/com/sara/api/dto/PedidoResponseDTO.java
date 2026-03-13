package com.sara.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Schema(description = "Resposta contendo os detalhes de um pedido")
public class PedidoResponseDTO {

    @Schema(description = "ID do pedido", example = "1")
    private Long id;

    @Schema(description = "ID do usuário do pedido", example = "1")
    private Long usuarioId;

    @Schema(description = "Nome do usuário (opcional para exibição)", example = "João da Silva")
    private String usuarioNome;

    @Schema(description = "ID da Forma de Pagamento", example = "1")
    private Long formaPagamentoId;

    @Schema(description = "Descrição da Forma de Pagamento", example = "PIX")
    private String formaPagamentoDescricao;

    @Schema(description = "Desconto total do pedido", example = "10.00")
    private BigDecimal desconto;

    @Schema(description = "Valor do frete", example = "15.00")
    private BigDecimal frete;

    @Schema(description = "Valor total final", example = "150.00")
    private BigDecimal valorTotal;

    @Schema(description = "Observação", example = "Entregue")
    private String observacao;

    @Schema(description = "Status de cancelamento", example = "false")
    private Boolean cancelado;

    @Schema(description = "Situação do pedido", example = "PENDENTE")
    private com.sara.api.model.SituacaoPedido situacao;

    @Schema(description = "Descrição formatada da situação", example = "Pendente")
    private String situacaoDescricao;

    @Schema(description = "Data e hora da criação do pedido", example = "2023-10-27T10:15:30")
    private LocalDateTime dataPedido;

    @Schema(description = "Indica se o pedido já foi pago", example = "false")
    private Boolean pago;

    @Schema(description = "Código PIX copia e cola", example = "0002010102122687...")
    private String pixCopiaECola;

    @Schema(description = "QR Code do PIX base64", example = "iVBORw0KGgoAAAANSUhEUgA...")
    private String pixQrCode;

    @Schema(description = "ID do pagamento no Mercado Pago", example = "123456789")
    private String mercadopagoPagamentoId;

    @Schema(description = "Indica se o pagamento será feito online", example = "true")
    private Boolean pagamentoOnline;

    @Schema(description = "Lista detalhada dos produtos no pedido")
    private List<PedidoProdutoResponseDTO> produtos;
}
