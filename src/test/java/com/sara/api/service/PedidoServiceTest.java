package com.sara.api.service;

import com.sara.api.dto.*;
import com.sara.api.model.*;
import com.sara.api.repository.*;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PedidoServiceTest {

    @Mock
    private PedidoRepository pedidoRepository;
    @Mock
    private UsuarioRepository usuarioRepository;
    @Mock
    private ProdutoRepository produtoRepository;
    @Mock
    private FormaPagamentoRepository formaPagamentoRepository;
    @Mock
    private EmailService emailService;
    @Mock
    private MercadoPagoService mercadoPagoService;

    @InjectMocks
    private PedidoService pedidoService;

    @Test
    @DisplayName("Deve buscar pedido por ID com sucesso")
    void deveBuscarPedidoPorIdComSucesso() {
        Long id = 1L;
        Pedido pedido = createPedidoMock(id);
        when(pedidoRepository.findById(id)).thenReturn(Optional.of(pedido));

        PedidoResponseDTO response = pedidoService.findById(id);

        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(id);
        verify(pedidoRepository).findById(id);
    }

    @Test
    @DisplayName("Deve lançar EntityNotFoundException ao buscar pedido inexistente")
    void deveLancarExceptionAoBuscarPedidoInexistente() {
        Long id = 99L;
        when(pedidoRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> pedidoService.findById(id))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("Pedido não encontrado");
    }

    @Test
    @DisplayName("Deve salvar um novo pedido com sucesso")
    void deveSalvarNovoPedidoComSucesso() {
        // GIVEN
        PedidoRequestDTO request = new PedidoRequestDTO();
        request.setUsuarioId(1L);
        request.setValorTotal(new BigDecimal("100.00"));
        
        PedidoProdutoRequestDTO itemRequest = new PedidoProdutoRequestDTO();
        itemRequest.setProdutoId(10L);
        itemRequest.setQuantidade(new BigDecimal("2.0"));
        itemRequest.setValor(new BigDecimal("50.00"));
        request.setProdutos(Collections.singletonList(itemRequest));

        Usuario usuario = new Usuario();
        usuario.setId(1L);
        usuario.setNome("Cliente Teste");

        Produto produto = new Produto();
        produto.setId(10L);
        produto.setNome("Produto Teste");
        produto.setPeso(1.0);

        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario));
        when(produtoRepository.findById(10L)).thenReturn(Optional.of(produto));
        
        Pedido pedidoSalvo = new Pedido();
        pedidoSalvo.setId(123L);
        pedidoSalvo.setUsuario(usuario);
        pedidoSalvo.setProdutos(new ArrayList<>());
        
        when(pedidoRepository.save(any(Pedido.class))).thenReturn(pedidoSalvo);
        when(pedidoRepository.findByIdWithProdutos(123L)).thenReturn(Optional.of(pedidoSalvo));

        // WHEN
        PedidoResponseDTO response = pedidoService.save(request);

        // THEN
        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(123L);
        verify(emailService).enviarEmailNovoPedido(any(Pedido.class));
        verify(pedidoRepository).save(any(Pedido.class));
    }

    @Test
    @DisplayName("Deve cancelar pedido e enviar e-mail")
    void deveCancelarPedidoComSucesso() {
        // GIVEN
        Long id = 1L;
        Pedido pedido = createPedidoMock(id);
        pedido.setCancelado(false);
        
        Usuario responsavel = new Usuario();
        responsavel.setNome("Admin");
        
        setupSecurityContext(responsavel);
        
        when(pedidoRepository.findById(id)).thenReturn(Optional.of(pedido));
        when(pedidoRepository.findByIdWithProdutos(id)).thenReturn(Optional.of(pedido));

        // WHEN
        pedidoService.cancel(id, "Motivo do cancelamento");

        // THEN
        assertThat(pedido.getCancelado()).isTrue();
        assertThat(pedido.getObservacao()).contains("Motivo do cancelamento");
        verify(pedidoRepository).save(pedido);
        verify(emailService).enviarEmailPedidoCancelado(eq(pedido), eq(responsavel), eq("Motivo do cancelamento"));
    }

    @Test
    @DisplayName("Deve sugerir frete baseado na tabela do usuário")
    void deveSugerirFreteDaTabelaDoUsuario() {
        Long usuarioId = 1L;
        Usuario usuario = new Usuario();
        TabelaFrete tabela = new TabelaFrete();
        tabela.setValor(new BigDecimal("25.50"));
        tabela.setAtivo(true);
        usuario.setTabelaFrete(tabela);

        when(usuarioRepository.findById(usuarioId)).thenReturn(Optional.of(usuario));

        TabelaFreteResponseDTO response = pedidoService.sugerirFrete(usuarioId);

        assertThat(response.getValor()).isEqualTo(new BigDecimal("25.50"));
    }

    private Pedido createPedidoMock(Long id) {
        Pedido pedido = new Pedido();
        pedido.setId(id);
        Usuario usuario = new Usuario();
        usuario.setId(10L);
        usuario.setNome("User");
        pedido.setUsuario(usuario);
        pedido.setProdutos(new ArrayList<>());
        pedido.setSituacao(SituacaoPedido.PENDENTE);
        return pedido;
    }

    private void setupSecurityContext(Usuario principal) {
        Authentication authentication = mock(Authentication.class);
        when(authentication.getPrincipal()).thenReturn(principal);
        SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);
    }
}
