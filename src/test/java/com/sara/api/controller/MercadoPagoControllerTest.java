package com.sara.api.controller;

import com.sara.api.model.Pagamento;
import com.sara.api.repository.PagamentoRepository;
import com.sara.api.service.MercadoPagoService;
import com.sara.api.service.PedidoService;
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
@AutoConfigureMockMvc(addFilters = false)
class MercadoPagoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private MercadoPagoService mercadoPagoService;

    @MockBean
    private PagamentoRepository pagamentoRepository;

    @MockBean
    private PedidoService pedidoService;

    @MockBean
    private TokenService tokenService;

    @MockBean
    private UsuarioRepository usuarioRepository;

    @Test
    @DisplayName("Deve retornar 200 e status do pagamento quando verificação for bem sucedida")
    @WithMockUser
    void deveRetornar200ESucessoNaVerificacao() throws Exception {
        // GIVEN
        Long pagamentoId = 1L;
        Pagamento pagamento = new Pagamento();
        pagamento.setId(pagamentoId);
        pagamento.setMercadopagoPagamentoId("MP123");
        pagamento.setPago(true);

        when(pagamentoRepository.findById(pagamentoId)).thenReturn(Optional.of(pagamento));
        when(mercadoPagoService.verificarStatusPagamento("MP123")).thenReturn("approved");

        // WHEN & THEN
        mockMvc.perform(post("/api/mercadopago/verificar-pagamento/{idPagamento}", pagamentoId)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("approved"))
                .andExpect(jsonPath("$.pago").value(true))
                .andExpect(jsonPath("$.idPagamento").value(pagamentoId));
    }

    @Test
    @DisplayName("Deve retornar 400 quando pagamento não tem ID do Mercado Pago")
    @WithMockUser
    void deveRetornar400QuandoSemIdPagamento() throws Exception {
        // GIVEN
        Long pagamentoId = 1L;
        Pagamento pagamento = new Pagamento();
        pagamento.setId(pagamentoId);
        pagamento.setMercadopagoPagamentoId(null);

        when(pagamentoRepository.findById(pagamentoId)).thenReturn(Optional.of(pagamento));

        // WHEN & THEN
        mockMvc.perform(post("/api/mercadopago/verificar-pagamento/{idPagamento}", pagamentoId)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Vencimento não possui ID de pagamento associado"));
    }

    @Test
    @DisplayName("Deve retornar 404 quando pagamento não for encontrado")
    @WithMockUser
    void deveRetornar404QuandoPagamentoInexistente() throws Exception {
        // GIVEN
        Long pagamentoId = 99L;
        when(pagamentoRepository.findById(pagamentoId)).thenReturn(Optional.empty());

        // WHEN & THEN
        mockMvc.perform(post("/api/mercadopago/verificar-pagamento/{idPagamento}", pagamentoId)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }
}
