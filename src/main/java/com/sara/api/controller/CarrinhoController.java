package com.sara.api.controller;

import com.sara.api.dto.CarrinhoRequestDTO;
import com.sara.api.dto.CarrinhoResponseDTO;
import com.sara.api.service.CarrinhoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/carrinho")
@Tag(name = "Carrinho", description = "Endpoints para gerenciamento do carrinho de compras dos usuários")
public class CarrinhoController {

    @Autowired
    private CarrinhoService carrinhoService;

    @PostMapping
    @Operation(summary = "Adicionar ao carrinho", description = "Vincula um produto ao carrinho de um usuário (Many-to-Many)")
    public ResponseEntity<CarrinhoResponseDTO> adicionar(
            @RequestBody CarrinhoRequestDTO dto) {
        return ResponseEntity.ok(carrinhoService.adicionar(dto));
    }

    @PostMapping("/lote")
    @Operation(summary = "Adicionar em lote", description = "Adiciona múltiplos produtos ao carrinho")
    public ResponseEntity<List<CarrinhoResponseDTO>> adicionarLote(
            @RequestBody List<CarrinhoRequestDTO> dtos) {
        return ResponseEntity.ok(carrinhoService.adicionarLote(dtos));
    }

    @DeleteMapping("/{idUsuario}/{idProduto}")
    @Operation(summary = "Remover do carrinho", description = "Remove vínculo de um produto com um usuário")
    public ResponseEntity<Void> remover(
            @PathVariable Long idUsuario,
            @PathVariable Long idProduto) {
        carrinhoService.remover(idUsuario, idProduto);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/usuario/{idUsuario}")
    @Operation(summary = "Limpar Todo o Carrinho", description = "Remove todos os produtos da sacola de compras do usuário")
    public ResponseEntity<Void> limparCarrinho(@PathVariable Long idUsuario) {
        carrinhoService.limparCarrinhoUsuario(idUsuario);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{idUsuario}/{idProduto}")
    @Operation(summary = "Atualizar quantidade", description = "Edita a quantidade de um produto no carrinho do usuário")
    public ResponseEntity<CarrinhoResponseDTO> atualizarQuantidade(
            @PathVariable Long idUsuario,
            @PathVariable Long idProduto,
            @RequestBody CarrinhoRequestDTO dto) {
        return ResponseEntity.ok(carrinhoService.atualizarQuantidade(idUsuario, idProduto, dto.getQuantidade()));
    }

    @GetMapping
    @Operation(summary = "Listar todos", description = "Traz o relacionamento de todos os carrinhos do banco")
    public List<CarrinhoResponseDTO> listarTodos() {
        return carrinhoService.listarTodos();
    }

    @GetMapping("/usuario/{idUsuario}")
    @Operation(summary = "Buscar por Usuário", description = "Busca a aba de carrinho ou itens de um usuário específico")
    public List<CarrinhoResponseDTO> buscarPorUsuario(@PathVariable Long idUsuario) {
        return carrinhoService.buscarPorUsuario(idUsuario);
    }

    @GetMapping("/produto/{idProduto}")
    @Operation(summary = "Buscar por Produto", description = "Lista quais usuários guardaram aquele produto específico no carrinho")
    public List<CarrinhoResponseDTO> buscarPorProduto(@PathVariable Long idProduto) {
        return carrinhoService.buscarPorProduto(idProduto);
    }
}
