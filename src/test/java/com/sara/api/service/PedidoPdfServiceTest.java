package com.sara.api.service;

import com.sara.api.model.Pedido;
import com.sara.api.model.PedidoProduto;
import com.sara.api.model.Produto;
import com.sara.api.model.Usuario;
import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PedidoPdfServiceTest {

    private final PedidoPdfService pdfService = new PedidoPdfService();

    @Test
    void shouldGeneratePdf() {
        Pedido pedido = new Pedido();
        pedido.setId(1L);
        pedido.setDataPedido(LocalDateTime.of(2026, 3, 18, 19, 0));
        
        Usuario usuario = new Usuario();
        usuario.setNome("Teste Cliente");
        usuario.setEndereco("Rua Teste");
        usuario.setNumero("123");
        usuario.setBairro("Centro");
        usuario.setCidade("São Paulo");
        usuario.setUf("SP");
        usuario.setCep("01001-000");
        pedido.setUsuario(usuario);
        
        List<PedidoProduto> produtos = new ArrayList<>();
        
        PedidoProduto pp1 = new PedidoProduto();
        Produto p1 = new Produto();
        p1.setNome("Camiseta Branca");
        p1.setTamanho(42);
        pp1.setProduto(p1);
        pp1.setQuantidade(new BigDecimal("8"));
        pp1.setValor(new BigDecimal("100.00"));
        produtos.add(pp1);
        
        PedidoProduto pp2 = new PedidoProduto();
        Produto p2 = new Produto();
        p2.setNome("Terço de Madeira");
        p2.setTamanho(0);
        pp2.setProduto(p2);
        pp2.setQuantidade(new BigDecimal("14"));
        pp2.setValor(new BigDecimal("10.00"));
        produtos.add(pp2);
        
        pedido.setProdutos(produtos);
        pedido.setValorTotal(new BigDecimal("940.00"));
        
        byte[] pdfBytes = pdfService.generatePedidoPdf(pedido);
        
        assertNotNull(pdfBytes);
        assertTrue(pdfBytes.length > 0);
    }
}
