package com.sara.api.controller;

import com.sara.api.model.Pedido;
import com.sara.api.repository.PedidoRepository;
import com.sara.api.service.MercadoPagoService;
import com.sara.api.repository.UsuarioRepository;
import com.sara.api.security.TokenService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(MercadoPagoController.class)
@AutoConfigureMockMvc(addFilters = false) // Desabilita filtros de segurança para facilitar o teste unitário de controller
class MercadoPagoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private MercadoPagoService mercadoPagoService;

    @MockBean
    private PedidoRepository pedidoRepository;

    @MockBean
    private TokenService tokenService;

    @MockBean
    private UsuarioRepository usuarioRepository;

    @Test
    @DisplayName("Deve retornar 200 e status do pagamento quando verificação for bem sucedida")
    @WithMockUser
    void deveRetornar200ESucessoNaVerificacao() throws Exception {
        // GIVEN
        Long pedidoId = 1L;
        Pedido pedido = new Pedido();
        pedido.setId(pedidoId);
        pedido.setMercadopagoPagamentoId("MP123");
        pedido.setPago(true);

        when(pedidoRepository.findById(pedidoId)).thenReturn(Optional.of(pedido));
        when(mercadoPagoService.verificarStatusPagamento("MP123")).thenReturn("approved");

        // WHEN & THEN
        mockMvc.perform(post("/api/mercadopago/verificar-pagamento/{idPedido}", pedidoId)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("approved"))
                .andExpect(jsonPath("$.pago").value(true))
                .andExpect(jsonPath("$.idPedido").value(pedidoId));
    }

    @Test
    @DisplayName("Deve retornar 400 quando pedido não tem ID do Mercado Pago")
    @WithMockUser
    void deveRetornar400QuandoSemIdPagamento() throws Exception {
        // GIVEN
        Long pedidoId = 1L;
        Pedido pedido = new Pedido();
        pedido.setId(pedidoId);
        pedido.setMercadopagoPagamentoId(null);

        when(pedidoRepository.findById(pedidoId)).thenReturn(Optional.of(pedido));

        // WHEN & THEN
        mockMvc.perform(post("/api/mercadopago/verificar-pagamento/{idPedido}", pedidoId)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Pedido não possui ID de pagamento associado"));
    }

    @Test
    @DisplayName("Deve retornar 404 quando pedido não for encontrado")
    @WithMockUser
    void deveRetornar404QuandoPedidoInexistente() throws Exception {
        // GIVEN
        Long pedidoId = 99L;
        when(pedidoRepository.findById(pedidoId)).thenReturn(Optional.empty());

        // WHEN & THEN
        mockMvc.perform(post("/api/mercadopago/verificar-pagamento/{idPedido}", pedidoId)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }
}
