package com.sara.api.controller;

import com.sara.api.model.Produto;
import com.sara.api.service.ProdutoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/produtos")
@Tag(name = "Produtos", description = "Endpoints para gestão do catálogo de produtos")
public class ProdutoController {

    @Autowired
    private ProdutoService produtoService;

    @PostMapping
    @Operation(summary = "Adicionar produto", description = "Cadastra um novo produto no catálogo")
    public Produto adicionar(@RequestBody Produto produto) {
        return produtoService.salvar(produto);
    }

    @GetMapping("/nome/{nome}")
    @Operation(summary = "Buscar por nome", description = "Retorna uma lista de produtos que contenham o nome informado")
    public List<Produto> buscarPorNome(@PathVariable("nome") String nome) {
        return produtoService.buscarPorNome(nome);
    }

    @GetMapping
    @Operation(summary = "Listar todos os produtos", description = "Retorna todos os produtos cadastrados no sistema")
    public List<Produto> listarTodos() {
        return produtoService.listarTodos();
    }

    @GetMapping("/ativos")
    @Operation(summary = "Listar produtos ativos", description = "Retorna todos os produtos ativos")
    public List<Produto> listarAtivos() {
        return produtoService.listarAtivos();
    }

    @GetMapping("/loja")
    @Operation(summary = "Pesquisa da Loja Vitrine", description = "Filtro avançado por nome, tamanho e faixa de preço")
    public List<Produto> buscarParaLoja(
            @RequestParam(required = false) String nome,
            @RequestParam(required = false) Integer tamanho,
            @RequestParam(required = false) Double precoMin,
            @RequestParam(required = false) Double precoMax) {
        return produtoService.buscarParaLoja(nome, tamanho, precoMin, precoMax);
    }

    @GetMapping("/codigo/{codigo}")
    @Operation(summary = "Buscar por código", description = "Retorna o produto ativo com o código informado")
    public ResponseEntity<Produto> buscarPorCodigo(@PathVariable("codigo") Long codigo) {
        return produtoService.buscarPorCodigo(codigo)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/{id}/outros-tamanhos")
    @Operation(summary = "Sugestão de Compra de Outros Tamanhos", description = "Busca produtos com o mesmo nome exato do produto logado, porém que têm um ID diferente.")
    public List<Produto> buscarOutrosTamanhos(@PathVariable("id") Long id) {
        return produtoService.buscarOutrosTamanhos(id);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Alterar produto", description = "Atualiza os dados de um produto existente por ID")
    public ResponseEntity<Produto> alterar(@PathVariable("id") Long id, @RequestBody Produto produto) {
        try {
            return ResponseEntity.ok(produtoService.alterar(id, produto));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluir(@PathVariable("id") Long id) {
        produtoService.excluir(id);
        return ResponseEntity.noContent().build();
    }
}
