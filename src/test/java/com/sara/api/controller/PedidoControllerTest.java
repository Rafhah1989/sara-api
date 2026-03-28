package com.sara.api.controller;

import com.sara.api.dto.PedidoRequestDTO;
import com.sara.api.dto.PedidoResponseDTO;
import com.sara.api.model.Pedido;
import com.sara.api.repository.UsuarioRepository;
import com.sara.api.security.TokenService;
import com.sara.api.service.PedidoPdfService;
import com.sara.api.service.PedidoService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;


import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(PedidoController.class)
@AutoConfigureMockMvc(addFilters = false)
class PedidoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PedidoService pedidoService;

    @MockBean
    private PedidoPdfService pedidoPdfService;

    @MockBean
    private TokenService tokenService;

    @MockBean
    private UsuarioRepository usuarioRepository;

    @Test
    @DisplayName("Deve retornar pedido por ID")
    @WithMockUser
    void deveRetornarPedidoPorId() throws Exception {
        Long id = 1L;
        PedidoResponseDTO response = new PedidoResponseDTO();
        response.setId(id);
        response.setUsuarioNome("Cliente Teste");

        when(pedidoService.findById(id)).thenReturn(response);

        mockMvc.perform(get("/api/pedidos/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id))
                .andExpect(jsonPath("$.usuarioNome").value("Cliente Teste"));
    }

    @Test
    @DisplayName("Deve criar um novo pedido")
    @WithMockUser
    void deveCriarNovoPedido() throws Exception {
        PedidoResponseDTO response = new PedidoResponseDTO();
        response.setId(100L);

        when(pedidoService.save(any(PedidoRequestDTO.class))).thenReturn(response);

        String json = """
                {
                    "usuarioId": 1,
                    "valorTotal": 150.00,
                    "produtos": []
                }
                """;

        mockMvc.perform(post("/api/pedidos")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(100L));
    }

    @Test
    @DisplayName("Deve cancelar um pedido")
    @WithMockUser
    void deveCancelarPedido() throws Exception {
        Long id = 1L;
        String motivo = "Erro no endereço";

        mockMvc.perform(delete("/api/pedidos/{id}", id)
                .param("motivo", motivo))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("Deve alterar situação do pedido")
    @WithMockUser
    void deveAlterarSituacao() throws Exception {
        Long id = 1L;
        String json = "{\"situacao\": \"EM_PRODUCAO\"}";

        mockMvc.perform(patch("/api/pedidos/{id}/situacao", id)
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("Deve gerar PDF do pedido")
    @WithMockUser
    void deveGerarPdf() throws Exception {
        Long id = 1L;
        Pedido pedido = new Pedido();
        byte[] pdfContent = "PDF Mock".getBytes();

        when(pedidoService.getPedidoEntityForPdf(id)).thenReturn(pedido);
        when(pedidoPdfService.generatePedidoPdf(pedido)).thenReturn(pdfContent);

        mockMvc.perform(get("/api/pedidos/{id}/pdf", id))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_PDF))
                .andExpect(header().string("Content-Disposition", "inline; filename=pedido_1.pdf"));
    }
}
