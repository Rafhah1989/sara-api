package com.sara.api.service;

import com.sara.api.dto.ProdutoMiniDTO;
import com.sara.api.dto.ProdutoResumoDTO;
import com.sara.api.model.Produto;
import com.sara.api.repository.ProdutoRepository;
import com.sara.api.exception.ValidationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import jakarta.persistence.criteria.Predicate;
import java.util.ArrayList;
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

    public List<ProdutoMiniDTO> buscarPorNomeMini(String nome) {
        return produtoRepository.findByNomeMini(nome);
    }

    public List<Produto> buscarPorNome(String nome) {
        return produtoRepository.findByNomeContainingIgnoreCase(nome);
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
                    produto.setPeso(produtoAtualizado.getPeso());
                    produto.setPreco(produtoAtualizado.getPreco());
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

    public Page<ProdutoResumoDTO> buscarParaLoja(String nome, Integer tamanho, Double precoMin, Double precoMax, Pageable pageable) {
        Specification<Produto> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.isTrue(root.get("ativo")));
            predicates.add(cb.isNotNull(root.get("preco")));

            if (nome != null && !nome.trim().isEmpty()) {
                predicates.add(cb.like(cb.lower(root.get("nome")), "%" + nome.toLowerCase() + "%"));
            }
            if (tamanho != null) {
                predicates.add(cb.equal(root.get("tamanho"), tamanho));
            }
            if (precoMin != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("preco"), precoMin));
            }
            if (precoMax != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("preco"), precoMax));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };

        // Garante que a ordenação seja estável adicionando nome e codigo como critérios secundários
        org.springframework.data.domain.Sort stableSort = pageable.getSort();
        if (stableSort.isSorted()) {
            stableSort = stableSort.and(org.springframework.data.domain.Sort.by("nome").ascending())
                                 .and(org.springframework.data.domain.Sort.by("codigo").ascending());
        } else {
            stableSort = org.springframework.data.domain.Sort.by("nome").ascending()
                                 .and(org.springframework.data.domain.Sort.by("codigo").ascending());
        }
        
        pageable = org.springframework.data.domain.PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), stableSort);

        return produtoRepository.findAll(spec, pageable).map(ProdutoResumoDTO::new);
    }

    public List<ProdutoMiniDTO> listarAtivosMini() {
        return produtoRepository.findAtivosMini();
    }

    public List<Produto> buscarOutrosTamanhos(Long id) {
        Produto produtoOriginal = produtoRepository.findById(id)
                .orElseThrow(() -> new ValidationException("Produto original não encontrado"));
        return produtoRepository.findByNomeAndAtivoTrueAndIdNot(produtoOriginal.getNome(), id);
    }

    public List<Produto> listarAtivosOrdenados() {
        return produtoRepository.findByAtivoTrueOrderByNomeAscCodigoAscTamanhoAsc();
    }
}
