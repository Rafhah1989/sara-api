package com.sara.api.controller;

import com.sara.api.dto.ProdutoMiniDTO;
import com.sara.api.dto.ProdutoResumoDTO;
import com.sara.api.model.Produto;
import com.sara.api.service.ProdutoPdfService;
import com.sara.api.service.ProdutoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/produtos")
@Tag(name = "Produtos", description = "Endpoints para gestão do catálogo de produtos")
public class ProdutoController {

    @Autowired
    private ProdutoService produtoService;

    @Autowired
    private ProdutoPdfService pdfService;

    @GetMapping("/catalogo")
    @Operation(summary = "Gerar catálogo PDF", description = "Gera um PDF com todos os produtos para rascunho de pedido")
    public ResponseEntity<byte[]> gerarCatalogo() {
        List<Produto> produtos = produtoService.listarAtivosOrdenados();
        byte[] pdf = pdfService.generateCatalogoPdf(produtos);

        return ResponseEntity.ok()
                .header("Content-Type", "application/pdf")
                .header("Content-Disposition", "attachment; filename=catalogo-produtos.pdf")
                .body(pdf);
    }

    @PostMapping
    @Operation(summary = "Adicionar produto", description = "Cadastra um novo produto no catálogo")
    public Produto adicionar(@RequestBody Produto produto) {
        return produtoService.salvar(produto);
    }

    @GetMapping("/nome/{nome}")
    @Operation(summary = "Buscar por nome", description = "Retorna uma lista de produtos que contenham o nome informado")
    public List<ProdutoMiniDTO> buscarPorNome(@PathVariable("nome") String nome) {
        return produtoService.buscarPorNomeMini(nome);
    }

    @GetMapping
    @Operation(summary = "Listar todos os produtos", description = "Retorna todos os produtos cadastrados no sistema")
    public List<Produto> listarTodos() {
        return produtoService.listarTodos();
    }

    @GetMapping("/ativos")
    @Operation(summary = "Listar produtos ativos (Mini)", description = "Retorna todos os produtos ativos sem a imagem Base64 para listagem rápida")
    public List<ProdutoMiniDTO> listarAtivos() {
        return produtoService.listarAtivosMini();
    }

    @GetMapping("/loja")
    @Operation(summary = "Pesquisa da Loja Vitrine (Paginada)", description = "Filtro avançado por nome, tamanho e faixa de preço com Lazy Loading")
    public Page<ProdutoResumoDTO> buscarParaLoja(
            @RequestParam(required = false) String nome,
            @RequestParam(required = false) Integer tamanho,
            @RequestParam(required = false) Double precoMin,
            @RequestParam(required = false) Double precoMax,
            @PageableDefault(size = 30, sort = "nome") Pageable pageable) {
        return produtoService.buscarParaLoja(nome, tamanho, precoMin, precoMax, pageable);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar detalhe do produto", description = "Retorna o produto completo, incluindo a imagem Base64")
    public ResponseEntity<Produto> buscarPorId(@PathVariable("id") Long id) {
        return produtoService.buscarPorId(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
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
