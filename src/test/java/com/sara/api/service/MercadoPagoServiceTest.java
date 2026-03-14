package com.sara.api.service;

import com.mercadopago.client.payment.PaymentClient;
import com.mercadopago.client.payment.PaymentCreateRequest;
import com.mercadopago.resources.payment.Payment;
import com.sara.api.model.Pedido;
import com.sara.api.model.Usuario;
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
    private PedidoRepository pedidoRepository;

    @InjectMocks
    private MercadoPagoService mercadoPagoService;

    @Test
    @DisplayName("Deve atualizar pedido para pago quando status for approved")
    void deveAtualizarPedidoParaPagoQuandoStatusForApproved() throws Exception {
        // GIVEN
        String paymentId = "123456";
        Payment mockPayment = mock(Payment.class);
        when(mockPayment.getStatus()).thenReturn("approved");

        Pedido pedido = new Pedido();
        pedido.setId(1L);
        pedido.setPago(false);

        when(pedidoRepository.findByMercadopagoPagamentoId(paymentId))
                .thenReturn(Optional.of(pedido));

        try (MockedConstruction<PaymentClient> mocked = mockConstruction(PaymentClient.class,
                (mock, context) -> {
                    when(mock.get(anyLong())).thenReturn(mockPayment);
                })) {
            
            // WHEN
            String status = mercadoPagoService.verificarStatusPagamento(paymentId);

            // THEN
            assertThat(status).isEqualTo("approved");
            assertThat(pedido.getPago()).isTrue();
            verify(pedidoRepository, times(1)).save(pedido);
        }
    }

    @Test
    @DisplayName("Não deve atualizar pedido quando status não for approved")
    void naoDeveAtualizarPedidoQuandoStatusNaoForApproved() throws Exception {
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
            verify(pedidoRepository, never()).save(any());
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
    @DisplayName("Deve criar pagamento PIX com expiração de 60 minutos")
    void deveCriarPagamentoPixComExpiracao() throws Exception {
        // GIVEN
        Pedido pedido = new Pedido();
        pedido.setId(1L);
        pedido.setValorTotal(new java.math.BigDecimal("100.00"));
        Usuario usuario = new Usuario();
        usuario.setEmail("test@test.com");
        usuario.setNome("Test User");
        usuario.setCpfCnpj("12345678901");
        pedido.setUsuario(usuario);

        Payment mockPayment = mock(Payment.class);

        try (MockedConstruction<PaymentClient> mocked = mockConstruction(PaymentClient.class,
                (mock, context) -> {
                    when(mock.create(any(PaymentCreateRequest.class))).thenReturn(mockPayment);
                })) {
            
            // WHEN
            Payment result = mercadoPagoService.criarPagamentoPix(pedido);

            // THEN
            assertThat(result).isEqualTo(mockPayment);
            
            // Verifica se a requisição enviada ao mock contém a expiração
            PaymentClient clientMock = mocked.constructed().get(0);
            verify(clientMock).create(argThat(request -> 
                request.getDateOfExpiration() != null && 
                request.getDateOfExpiration().isAfter(java.time.OffsetDateTime.now().plusMinutes(14))
            ));
        }
    }
}
