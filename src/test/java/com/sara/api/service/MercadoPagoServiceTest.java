package com.sara.api.service;

import com.mercadopago.client.payment.PaymentClient;
import com.mercadopago.client.payment.PaymentCreateRequest;
import com.mercadopago.resources.payment.Payment;
import com.sara.api.model.Pagamento;
import com.sara.api.model.Pedido;
import com.sara.api.model.Usuario;
import com.sara.api.repository.PagamentoRepository;
import com.sara.api.repository.PedidoRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedConstruction;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MercadoPagoServiceTest {

    @Mock
    private PagamentoRepository pagamentoRepository;

    @Mock
    private PedidoRepository pedidoRepository;

    @Mock
    private EmailService emailService;

    @Mock
    private PedidoService pedidoService;

    @InjectMocks
    private MercadoPagoService mercadoPagoService;

    @Test
    @DisplayName("Deve atualizar pagamento para pago quando status for approved")
    void deveAtualizarPagamentoParaPagoQuandoStatusForApproved() throws Exception {
        // GIVEN
        String paymentId = "123456";
        Payment mockPayment = mock(Payment.class);
        when(mockPayment.getStatus()).thenReturn("approved");

        Pagamento pagamento = new Pagamento();
        pagamento.setId(1L);
        pagamento.setPago(false);
        Pedido pedido = new Pedido();
        pedido.setId(10L);
        pagamento.setPedido(pedido);

        when(pagamentoRepository.findByMercadopagoPagamentoId(paymentId))
                .thenReturn(Optional.of(pagamento));

        try (MockedConstruction<PaymentClient> mocked = mockConstruction(PaymentClient.class,
                (mock, context) -> {
                    when(mock.get(anyLong())).thenReturn(mockPayment);
                })) {
            
            // WHEN
            String status = mercadoPagoService.verificarStatusPagamento(paymentId);

            // THEN
            assertThat(status).isEqualTo("approved");
            assertThat(pagamento.getPago()).isTrue();
            verify(pagamentoRepository, times(1)).save(pagamento);
        }
    }

    @Test
    @DisplayName("Não deve atualizar pagamento quando status não for approved")
    void naoDeveAtualizarPagamentoQuandoStatusNaoForApproved() throws Exception {
        // GIVEN
        String paymentId = "123456";
        Payment mockPayment = mock(Payment.class);
        when(mockPayment.getStatus()).thenReturn("pending");

        try (MockedConstruction<PaymentClient> mocked = mockConstruction(PaymentClient.class,
                (mock, context) -> {
                    when(mock.get(anyLong())).thenReturn(mockPayment);
                })) {
            
            // WHEN
            String status = mercadoPagoService.verificarStatusPagamento(paymentId);

            // THEN
            assertThat(status).isEqualTo("pending");
            verify(pagamentoRepository, never()).save(any());
        }
    }

    @Test
    @DisplayName("Deve retornar mensagem de erro quando ocorrer exceção")
    void deveRetornarMensagemDeErroQuandoOcorrerExcecao() {
        // GIVEN
        String paymentId = "123456";

        try (MockedConstruction<PaymentClient> mocked = mockConstruction(PaymentClient.class,
                (mock, context) -> {
                    when(mock.get(anyLong())).thenThrow(new RuntimeException("API Error"));
                })) {
            
            // WHEN
            String status = mercadoPagoService.verificarStatusPagamento(paymentId);

            // THEN
            assertThat(status).contains("Erro: API Error");
        }
    }

    @Test
    @DisplayName("Deve criar pagamento PIX para uma parcela")
    void deveCriarPagamentoPixParaParcela() throws Exception {
        // GIVEN
        Pedido pedido = new Pedido();
        pedido.setId(10L);
        Usuario usuario = new Usuario();
        usuario.setEmail("test@test.com");
        usuario.setNome("Test User");
        usuario.setCpfCnpj("12345678901");
        pedido.setUsuario(usuario);

        Pagamento pagamento = new Pagamento();
        pagamento.setId(1L);
        pagamento.setPedido(pedido);
        pagamento.setValor(new java.math.BigDecimal("100.00"));

        Payment mockPayment = mock(Payment.class);

        try (MockedConstruction<PaymentClient> mocked = mockConstruction(PaymentClient.class,
                (mock, context) -> {
                    when(mock.create(any(PaymentCreateRequest.class))).thenReturn(mockPayment);
                })) {
            
            // WHEN
            Payment result = mercadoPagoService.criarPagamentoPix(pagamento);

            // THEN
            assertThat(result).isEqualTo(mockPayment);
            
            PaymentClient clientMock = mocked.constructed().get(0);
            verify(clientMock).create(argThat(request -> 
                request.getTransactionAmount().compareTo(new java.math.BigDecimal("100.00")) == 0 &&
                request.getDescription().contains("Parcela #1")
            ));
        }
    }
}
