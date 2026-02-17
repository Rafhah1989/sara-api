package com.sara.api.service;

import com.sara.api.model.Produto;
import com.sara.api.repository.ProdutoRepository;
import com.sara.api.exception.ValidationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ProdutoService {

    @Autowired
    private ProdutoRepository produtoRepository;

    public Produto salvar(Produto produto) {
        validar(produto);
        return produtoRepository.save(produto);
    }

    public List<Produto> buscarPorNome(String nome) {
        return produtoRepository.findByNomeContainingIgnoreCaseAndAtivoTrue(nome);
    }

    public List<Produto> listarAtivos() {
        return produtoRepository.findByAtivoTrue();
    }

    public Optional<Produto> buscarPorCodigo(Long codigo) {
        return produtoRepository.findByCodigoAndAtivoTrue(codigo);
    }

    public List<Produto> listarTodos() {
        return produtoRepository.findAll();
    }

    public Optional<Produto> buscarPorId(Long id) {
        return produtoRepository.findById(id);
    }

    public Produto alterar(Long id, Produto produtoAtualizado) {
        validar(produtoAtualizado);
        return produtoRepository.findById(id)
                .map(produto -> {
                    produto.setNome(produtoAtualizado.getNome());
                    produto.setTamanho(produtoAtualizado.getTamanho());
                    produto.setAtivo(produtoAtualizado.getAtivo());
                    produto.setImagem(produtoAtualizado.getImagem());
                    produto.setCodigo(produtoAtualizado.getCodigo());
                    return produtoRepository.save(produto);
                })
                .orElseThrow(() -> new ValidationException("Produto não encontrado com id: " + id));
    }

    private void validar(Produto produto) {
        if (produto.getNome() == null || produto.getNome().trim().isEmpty()) {
            throw new ValidationException("O nome do produto é obrigatório");
        }
    }

    public void excluir(Long id) {
        produtoRepository.deleteById(id);
    }
}
